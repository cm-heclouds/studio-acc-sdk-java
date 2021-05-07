package com.onenet.studio.acc.sdk.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.auto.service.AutoService;
import com.onenet.studio.acc.sdk.annotations.ThingsModelConfiguration;
import com.onenet.studio.acc.sdk.interfaces.OpenApiCallback;
import com.onenet.studio.acc.sdk.processor.enums.AccessMode;
import com.onenet.studio.acc.sdk.processor.type.ArrayType;
import com.onenet.studio.acc.sdk.util.OneJsonUtil;
import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 物模型自定义注解处理器
 *
 * @author wjl
 * @date 2020-12-21
 */
@AutoService(Processor.class)
public class OpenApiExtentionProcessor extends AbstractProcessor {

    private static final String STRUCT_DTO_SUFFIX = "StructDTO";

    private static final String PACKAGE_DTO = "com.onenet.studio.acc.sdk.dto";

    private static final String PACKAGE_EXTENTION = "com.onenet.studio.acc.sdk";

    private ClassName classOpenApi = ClassName.get("com.onenet.studio.acc.sdk", "OpenApi");

    private static final String PROPERTY_UPLOAD = "propertyUpload";

    private static final String PROPERTY_SET = "propertySet";

    private static final String PROPERTY_SET_REPLY = "propertySetReply";

    private static final String PROPERTY_GET = "propertyGet";

    private static final String PROPERTY_GET_REPLY = "propertyGetReply";

    private static final String DESIRED_GET = "propertyDesiredGet";

    private static final String DESIRED_DEL = "propertyDesiredDel";

    private static final String EVENT_UP = "eventUpload";

    private static final String SERVICE_INVOKE = "ServiceInvoke";

    private static final String SERVICE_INVOKE_REPLY = "ServiceInvokeReply";


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(ThingsModelConfiguration.class.getCanonicalName());
        return annotataions;
    }

    /**
     * 注解处理方法
     *
     * @author wjl
     * @date 2020/12/28
     *
     **/
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ThingsModelConfiguration.class);
        for (Element element : elements) {
            if (ElementKind.CLASS == element.getKind()) {
                processProductConfiguration(element);
            }
        }
        return true;
    }

    /**
     * 读取物模型json文件生成OpenApiExtention
     *
     * @author wjl
     * @date 2020/12/28
     *
     **/
    public void processProductConfiguration(Element element) {
        try {
            String configPath = element.getAnnotation(ThingsModelConfiguration.class).value();
            String config = readFileToString(configPath);
            Functions functions = JSON.parseObject(config, Functions.class);

            // OpenApiExtention 构造函数
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(classOpenApi, "openApi")
                    .addStatement("this.$N = $N", "openApi", "openApi")
                    .build();

            // 构建 OpenApiExtention 类
            List<FieldSpec> fieldSpecList = new ArrayList<>();
            fieldSpecList.add(FieldSpec.builder(String.class, "ONEJSON_ID_KEY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("物模型上报格式公共字段-id")
                    .initializer("\"id\"").build());
            fieldSpecList.add(FieldSpec.builder(String.class, "ONEJSON_VERSION_KEY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("物模型上报格式公共字段-version")
                    .initializer("\"version\"").build());
            fieldSpecList.add(FieldSpec.builder(String.class, "ONEJSON_VERSION_VAL", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("物模型上报格式公共字段-version的值")
                    .initializer("\"1.0\"").build());
            List<MethodSpec> methodSpecs = new ArrayList<>();
            methodSpecs.addAll(processProperties(functions.getProperties()));
            methodSpecs.addAll(processEvents(functions.getEvents()));
            methodSpecs.addAll(processServices(functions.getServices()));
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder("OpenApiExtention")
                    .addJavadoc("编译器自动生成，请勿修改<p/>\n")
                    .addJavadoc("\n")
                    .addJavadoc("{@link OpenApi} 扩展类，该类根据配置的物模型自动生成对应的上报与下发方法，开发者根据这些方法实现相应功能即可\n")
                    .addFields(fieldSpecList)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethods(methodSpecs)
                    .addMethod(constructor)
                    .addField(classOpenApi, "openApi", Modifier.PRIVATE);

            JavaFile apiExtentionFile = JavaFile.builder(PACKAGE_EXTENTION, classBuilder.build()).indent("    ").build();
            apiExtentionFile.writeTo(processingEnv.getFiler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 属性类型处理
     *
     * @author wjl
     * @date 2020/12/25
     * @param properties 物模型属性类型
     * @return 生成的所有属性相关方法
     **/
    public List<MethodSpec> processProperties(List<ThingsProperties> properties) throws Exception {
        // 属性上报
        List<MethodSpec> propertyMethods = new ArrayList<>(processPropertyUpload(properties));
        // 期望值获取
        propertyMethods.add(processPropertyDesiredGet());
        // 期望值删除
        propertyMethods.add(processPropertyDesiredDel());
        for (ThingsProperties property : properties) {
            if (AccessMode.RW.getMode().equals(property.getAccessMode())) {
                // 属性设置
                propertyMethods.add(processPropertySet());
                // 属性设置返回
                propertyMethods.add(processPropertySetReply());
                // 属性获取
                propertyMethods.add(processPropertyGet());
                // 属性获取返回
                propertyMethods.add(processPropertyGetReply());
                break;
            }
        }
        return propertyMethods;
    }

    /**
     * 事件类型处理
     *
     * @author wjl
     * @date 2020/12/25
     * @param events 物模型事件类型
     * @return 生成的所有事件相关方法
     **/
    public List<MethodSpec> processEvents(List<ThingsEvents> events) throws Exception {
        List<MethodSpec> eventMethods = new ArrayList<>();
        eventMethods.add(processEventUp(events));

        return eventMethods;
    }

    /**
     * 服务类型处理
     *
     * @author wjl
     * @date 2020/12/28
     * @param services 物模型服务类型
     * @return 生成的所有服务相关方法
     **/
    public List<MethodSpec> processServices(List<ThingsServices> services) throws Exception {
        List<MethodSpec> serviceMethods = new ArrayList<>();
        serviceMethods.addAll(processServiceInvoke(services));
        serviceMethods.addAll(processServiceInvokeReply(services));
        return serviceMethods;
    }

    /**
     * 根据物模型定义的type获取对应的java数据类型，因需要做是否为null判断，故使用封箱类型
     *
     * @author wjl
     * @date 2020/12/28
     * @param type 物模型dataType中的type类型
     * @return java封箱的数据类型
     **/
    public Type getClassTypeByDataType(String type) throws Exception {
        switch (type) {
            case "int32":
            case "enum" :
                return Integer.class;
            case "int64":
            case "bitMap" :
            case "date" :
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            case "bool":
                return Boolean.class;
            case "string":
                return String.class;
            default:
                throw new Exception("invalid dataType : " + type);
        }
    }

    /**
     * dto生成类，当物模型的属性类型为struct，或有事件类型、服务类型时调用该方法
     *
     * @author wjl
     * @date 2020/12/28
     * @param identifier 功能点标识符
     * @param fieldSpecs dto中的属性
     **/
    public void buildStructDto(String identifier, List<FieldSpec> fieldSpecs) throws IOException {
        List<MethodSpec> setGetMethods = new ArrayList<>();
        // add set methods
        for (FieldSpec fieldSpec : fieldSpecs) {
            MethodSpec set = MethodSpec.methodBuilder("set" + OneJsonUtil.firstCharUpperCase(fieldSpec.name))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(fieldSpec.type, fieldSpec.name)
                    .addStatement("this.$N = $N", fieldSpec.name, fieldSpec.name)
                    .build();
            setGetMethods.add(set);
        }

        // add get methods
        for (FieldSpec fieldSpec : fieldSpecs) {
            MethodSpec get = MethodSpec.methodBuilder("get" + OneJsonUtil.firstCharUpperCase(fieldSpec.name))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(fieldSpec.type)
                    .addStatement("return this." + fieldSpec.name)
                    .build();
            setGetMethods.add(get);
        }

        TypeSpec dtoClass = TypeSpec.classBuilder(identifier + STRUCT_DTO_SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addMethods(setGetMethods)
                .addFields(fieldSpecs)
                .build();
        JavaFile javaFile = JavaFile.builder(PACKAGE_DTO, dtoClass)
                .build();
        javaFile.writeTo(processingEnv.getFiler());
    }

    /**
     * 处理属性功能点上报
     *
     * @author wjl
     * @date 2020/12/23
     * @param properties 属性功能点
     * @return 构建属性上报方法（单个属性上报和多属性上报）
     *
     **/
    public List<MethodSpec> processPropertyUpload(List<ThingsProperties> properties) throws Exception {
        // 包含1.每个功能点一个方法；2.所有功能点一个方法
        List<MethodSpec> upMethods = new ArrayList<>();
        MethodSpec.Builder builder = MethodSpec.methodBuilder(PROPERTY_UPLOAD)
                .addJavadoc("设备属性上报\n")
                .addJavadoc("该方法会上报参数值不为null的属性\n")
                .addJavadoc("\n")
                .addJavadoc(" @param timeout 调用超时时间，单位为毫秒\n");

        List<ParameterSpec> params = new ArrayList<>();
        params.add(ParameterSpec.builder(long.class, "timeout").build());
        // 参数为自定义对象时需json序列化，故此处map的key为identifier,val为oneJson的值
        Map<String, String> map = new HashMap<>();
        // 属性名的identifier和真实物模型identifier可能不一致，key为属性名的identifier,val为物模型的identifier
        Map<String, String> identifierMap = new HashMap<>();
        for (int i = 0; i < properties.size(); i++) {
            ThingsProperties property = properties.get(i);
            String identifier = property.getIdentifier().replace("-", "");
            builder.addJavadoc(" @param $N 标识符为$N的属性功能点的值\n", identifier, property.getIdentifier());
            String type = property.getDataType().getType();
            Object specs = property.getDataType().getSpecs();
            identifierMap.put(identifier, property.getIdentifier());
            switch (type) {
                case "struct":
                    if (specs instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) specs;
                        List<InOrOutputData> specsList = jsonArray.toJavaList(InOrOutputData.class);
                        List<FieldSpec> fieldSpecList = new ArrayList<>();
                        for (InOrOutputData data : specsList) {
                            if ("array".equals(data.getDataType().getType()) || "struct".equals(data.getDataType().getType())) {
                                throw new Exception("invalid dataType of identifier : " + data.getParamIdentifier());
                            }
                            String paramIdentifier = data.getParamIdentifier().replace("-", "");
                            AnnotationSpec annotation = AnnotationSpec.builder(JSONField.class).addMember("name", "$S", data.getParamIdentifier()).build();
                            fieldSpecList.add(FieldSpec.builder(getClassTypeByDataType(data.getDataType().getType()), paramIdentifier, Modifier.PRIVATE).addAnnotation(annotation).build());
                        }
                        String dtoName = identifier;
                        if (!dtoName.startsWith("$")) {
                            dtoName = OneJsonUtil.firstCharUpperCase(dtoName);
                        }
                        buildStructDto(dtoName, fieldSpecList);
                        ClassName classDto = ClassName.get("com.onenet.studio.acc.sdk.dto", dtoName + STRUCT_DTO_SUFFIX);
                        params.add(ParameterSpec.builder(classDto, identifier).build());
                        map.put(identifier, "JSON.toJSON(" + identifier + ")");
                        // 生成单个功能点方法
                        upMethods.add(generateSinglePropUp(identifier, property.getIdentifier(), classDto, i, properties.size()));
                    } else {
                        throw new Exception("invalid struct specs of identifier : " + identifier);
                    }
                    break;
                case "array":
                    ArrayType arrayType = JSON.parseObject(JSON.toJSONString(specs), ArrayType.class);
                    if ("array".equals(arrayType.getType())) {
                        throw new Exception("invalid arrayType : " + arrayType.getType());
                    } else if ("struct".equals(arrayType.getType())) {
                        Object arraySpecs = arrayType.getSpecs();
                        if (arraySpecs instanceof JSONArray) {
                            JSONArray jsonArray = (JSONArray) arraySpecs;
                            List<InOrOutputData> specsList = jsonArray.toJavaList(InOrOutputData.class);
                            List<FieldSpec> fieldSpecList = new ArrayList<>();
                            for (InOrOutputData data : specsList) {
                                if ("array".equals(data.getDataType().getType()) || "struct".equals(data.getDataType().getType())) {
                                    throw new Exception("invalid dataType of identifier : " + data.getParamIdentifier());
                                }
                                String paramIdentifier = data.getParamIdentifier().replace("-", "");
                                AnnotationSpec annotation = AnnotationSpec.builder(JSONField.class).addMember("name", "$S", data.getParamIdentifier()).build();
                                fieldSpecList.add(FieldSpec.builder(getClassTypeByDataType(data.getDataType().getType()), paramIdentifier, Modifier.PRIVATE).addAnnotation(annotation).build());
                            }
                            String dtoName = identifier;
                            if (!dtoName.startsWith("$")) {
                                dtoName = OneJsonUtil.firstCharUpperCase(dtoName);
                            }
                            buildStructDto(dtoName, fieldSpecList);
                            ClassName classDto = ClassName.get("com.onenet.studio.acc.sdk.dto", dtoName + STRUCT_DTO_SUFFIX);
                            params.add(ParameterSpec.builder(ArrayTypeName.of(classDto), identifier).build());
                            map.put(identifier, "JSON.toJSON(" + identifier + ")");
                            // 生成单个功能点方法
                            upMethods.add(generateSinglePropUp(identifier, property.getIdentifier(), ArrayTypeName.of(classDto), i, properties.size()));
                        } else {
                            throw new Exception("invalid struct data of identifier : " + identifier);
                        }
                    } else {
                        ArrayTypeName arrayTypeName = ArrayTypeName.of(getClassTypeByDataType(arrayType.getType()));
                        params.add(ParameterSpec.builder(arrayTypeName, identifier).build());
                        map.put(identifier, identifier);
                        // 生成单个功能点方法
                        upMethods.add(generateSinglePropUp(identifier, property.getIdentifier(), arrayTypeName, i, properties.size()));
                    }
                    break;
                default:
                    params.add(ParameterSpec.builder(getClassTypeByDataType(type), identifier).build());
                    map.put(identifier, identifier);
                    // 生成单个功能点方法
                    upMethods.add(generateSinglePropUp(identifier, property.getIdentifier()
                            , getClassTypeByDataType(type), i, properties.size()));
                    break;
            }
        }
        builder.addParameters(params)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addException(Exception.class);
        OneJsonUtil.propertyUpOneJson(builder, map, identifierMap);
        builder.addStatement("return this.openApi.propertyPost(id, oneJsonStr, timeout)");
        upMethods.add(builder.build());
        return upMethods;
    }

    /**
     * 属性期望值获取
     *
     * @author wjl
     * @date 2020/12/24
     * @return 构建期望值获取方法
     **/
    public MethodSpec processPropertyDesiredGet() {
        String paramName = "identifiers";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(DESIRED_GET)
                .addJavadoc("设备期望值获取\n")
                .addJavadoc("\n")
                .addJavadoc(" @param " + paramName + " 要获取期望值的属性的标识符(identifier)数组\n")
                .addJavadoc(" @param timeout 调用超时时间，单位为毫秒")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String[].class, paramName)
                .addParameter(long.class, "timeout")
                .returns(JSONObject.class)
                .addException(Exception.class);
        OneJsonUtil.desiredGet(methodBuilder, paramName);
        methodBuilder.addStatement("return this.openApi.desiredGet(id, oneJsonStr, timeout)");
        return methodBuilder.build();
    }

    /**
     * 删除设备属性期望值
     *
     * @author wjl
     * @date 2020/12/24
     * @return 构建期望值删除方法
     **/
    public MethodSpec processPropertyDesiredDel() {
        String paramName = "identifiers";
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(DESIRED_DEL)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("删除设备属性期望值\n")
                .addJavadoc("\n")
                .addJavadoc(" @param identifiers 要删除的期望值属性的标识符(String)和版本(int)的map，版本可在期望值获取时可知\n")
                .addJavadoc(" @param timeout 调用超时时间，单位为毫秒")
                .addParameter(long.class, "timeout")
                .addParameter(Map.class, paramName)
                .returns(int.class)
                .addException(Exception.class);
        OneJsonUtil.desiredDel(methodBuilder, paramName);
        methodBuilder.addStatement("return this.openApi.desiredDel(id, oneJsonStr, timeout)");
        return methodBuilder.build();
    }

    /**
     * 设备属性设置（同步）,下行消息
     *
     * @author wjl
     * @date 2020/12/24
     * @return 构建属性设置方法
     **/
    public MethodSpec processPropertySet() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(PROPERTY_SET)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("设备属性设置订阅\n")
                .addJavadoc("\n")
                .addJavadoc(" @param callback 设备接收消息回调方法，由用户自定义实现")
                .addParameter(OpenApiCallback.class, "callback")
                .addStatement("this.openApi.propertySetSubscribe(callback)")
                .returns(void.class)
                .addException(Exception.class);
        return builder.build();
    }

    /**
     * 设备属性设置返回
     *
     * @author wjl
     * @date 2020/12/25
     * @return 构建服务设置返回方法
     **/
    public MethodSpec processPropertySetReply() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(PROPERTY_SET_REPLY)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("设备属性设置返回结果方法\n")
                .addJavadoc("\n")
                .addJavadoc(" @param messageId 消息id，设备属性设置中获取到的字段id的值\n")
                .addJavadoc(" @param code 返回状态码，200为成功\n")
                .addJavadoc(" @param msg 返回消息\n")
                .addParameter(String.class, "messageId")
                .addParameter(Integer.class, "code")
                .addParameter(String.class, "msg")
                .returns(void.class)
                .addException(Exception.class);
        OneJsonUtil.getBaseReplyOneJson(builder);
        builder.addStatement("String replyStr = JSON.toJSONString(map)")
                .addStatement("this.openApi.propertySetPublish(replyStr)");
        return builder.build();
    }

    /**
     * 设备属性获取
     *
     * @author wjl
     * @date 2020/12/25
     * @return 构建属性获取订阅方法
     **/
    public MethodSpec processPropertyGet() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(PROPERTY_GET)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("设备属性d获取订阅\n")
                .addJavadoc("\n")
                .addJavadoc(" @param callback 设备接收消息回调方法，由用户自定义实现")
                .addParameter(OpenApiCallback.class, "callback")
                .addStatement("this.openApi.propertyGetSubscribe(callback)")
                .returns(void.class)
                .addException(Exception.class);
        return builder.build();
    }

    /**
     * 设备属性获取返回
     *
     * @author wjl
     * @date 2020/12/25
     * @return 构建服务获取返回方法
     *
     **/
    public MethodSpec processPropertyGetReply() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(PROPERTY_GET_REPLY)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("设备属性获取返回结果方法\n")
                .addJavadoc("\n")
                .addJavadoc(" @param messageId 消息id，设备属性设置中获取到的字段id的值\n")
                .addJavadoc(" @param code 返回状态码，200为成功\n")
                .addJavadoc(" @param msg 返回消息\n")
                .addJavadoc(" @param data 属性值数据，key为标识符(identifier),value为对应的数据值")
                .addParameter(String.class, "messageId")
                .addParameter(Integer.class, "code")
                .addParameter(String.class, "msg")
                .addParameter(Map.class, "data")
                .returns(void.class)
                .addException(Exception.class);
        OneJsonUtil.getBaseReplyOneJson(builder);

        builder.addStatement("map.put(\"data\", JSON.toJSON($N))", "data")
                .addStatement("String replyStr = JSON.toJSONString(map)")
                .addStatement("this.openApi.propertyGetPublish(replyStr)");
        return builder.build();
    }

    /**
     *
     *
     * @author wjl
     * @date 2020/12/25
     * @param events 属性事件
     * @return 事件上报方法
     **/
    public MethodSpec processEventUp(List<ThingsEvents> events) throws Exception {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(EVENT_UP)
            .addJavadoc("设备事件上报\n")
            .addJavadoc("\n")
            .addJavadoc(" @param timeout 调用超时时间,单位为毫秒\n");
        List<ParameterSpec> params = new ArrayList<>();
        params.add(ParameterSpec.builder(long.class, "timeout").build());
        // 参数为自定义对象时需json序列化，故此处map的key为identifier,val为oneJson的值
        Map<String, String> map = new HashMap<>();
        // 属性名的identifier和真实物模型identifier可能不一致，key为属性名的identifier,val为物模型的identifier
        Map<String, String> identifierMap = new HashMap<>();

        for (ThingsEvents event : events) {
            String identifier = event.getIdentifier().replace("-", "");
            List<InOrOutputData> outputDatas = event.getOutputData();
            List<FieldSpec> outputFields = new ArrayList<>();
            buildInOrOutputDto(outputDatas, outputFields, identifier);

            map.put(identifier, "JSON.toJSON(" + identifier + ")");
            identifierMap.put(identifier, event.getIdentifier());
            String dtoName = identifier;
            if (!dtoName.startsWith("$")) {
                dtoName = OneJsonUtil.firstCharUpperCase(dtoName);
            }
            buildStructDto(dtoName, outputFields);
            ClassName classDto = ClassName.get("com.onenet.studio.acc.sdk.dto", dtoName + STRUCT_DTO_SUFFIX);
            params.add(ParameterSpec.builder(classDto, identifier).build());
            builder.addJavadoc(" @param " + identifier + " 事件输出标识符为" + identifier +"的参数\n");
        }
        builder.addParameters(params)
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addException(Exception.class);
        OneJsonUtil.eventUpOneJson(builder, map, identifierMap);
        builder.addStatement("return this.openApi.eventPost(id, oneJsonStr, timeout)");
        return builder.build();
    }

    /**
     * 服务调用订阅
     *
     * @author wjl
     * @date 2020/12/25
     * @param services 物模型服务
     * @return 构建的多个服务调用订阅方法
     **/
    public List<MethodSpec> processServiceInvoke(List<ThingsServices> services) {
        List<MethodSpec> methods = new ArrayList<>();
        for (ThingsServices service : services) {
            String identifier = service.getIdentifier().replace("-", "");
            MethodSpec.Builder builder = MethodSpec.methodBuilder(identifier + SERVICE_INVOKE)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("标识符为$N的设备服务调用订阅\n", service.getIdentifier())
                    .addJavadoc("\n")
                    .addJavadoc(" @param callback 设备接收消息回调方法，由用户自定义实现\n")
                    .addParameter(OpenApiCallback.class, "callback")
                    .returns(void.class)
                    .addException(Exception.class);
            builder.addStatement("this.openApi.serviceInvokeSubscribe($S, callback)", service.getIdentifier());
            methods.add(builder.build());
        }
        return methods;
    }

    /**
     * 服务调用回复
     *
     * @author wjl
     * @date 2020/12/25
     * @param services 物模型服务
     * @return 构建多个服务返回方法
     **/
    public List<MethodSpec> processServiceInvokeReply(List<ThingsServices> services) throws Exception {
        List<MethodSpec> methods = new ArrayList<>();
        for (ThingsServices service : services) {
            String identifier = service.getIdentifier().replace("-", "");
            List<InOrOutputData> outputDatas = service.getOutput();
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(identifier + SERVICE_INVOKE_REPLY)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addException(Exception.class)
                .addJavadoc(" @param messageId 消息id，设备属性设置中获取到的字段id的值\n")
                .addJavadoc(" @param code 返回状态码，200为成功\n")
                .addJavadoc(" @param msg 返回消息\n")
                .addParameter(String.class, "messageId")
                .addParameter(Integer.class, "code")
                .addParameter(String.class, "msg");

            List<FieldSpec> outputFields = new ArrayList<>();
            buildInOrOutputDto(outputDatas, outputFields, identifier);
            String dtoName = identifier;
            if (!dtoName.startsWith("$")) {
                dtoName = OneJsonUtil.firstCharUpperCase(dtoName);
            }
            buildStructDto(dtoName, outputFields);
            ClassName classDto = ClassName.get("com.onenet.studio.acc.sdk.dto", dtoName + STRUCT_DTO_SUFFIX);
            methodBuilder.addJavadoc(" @param $N 标识符为$N的服务输出参数\n", identifier, service.getIdentifier());
            methodBuilder.addParameter(ParameterSpec.builder(classDto, identifier).build());

            OneJsonUtil.serviceInvokeReply(methodBuilder, "JSON.toJSON(" + identifier + ")");
            methodBuilder.addStatement("this.openApi.serviceInvokePublish($S, oneJsonStr)", service.getIdentifier());
            methods.add(methodBuilder.build());
        }
        return methods;
    }

    /**
     * 物模型为事件或服务类型时生成输出参数的dto
     *
     * @author wjl
     * @date 2020/12/28
     * @param outputDatas 输出参数
     * @param outputFileds dto中的属性
     * @param identifier 物模型的标识符
     **/
    public void buildInOrOutputDto(List<InOrOutputData> outputDatas, List<FieldSpec> outputFileds, String identifier) throws Exception {
        for (InOrOutputData outputDatadata : outputDatas) {
            String outputIdentifier = outputDatadata.getParamIdentifier().replace("-", "");
            String outputFileName = OneJsonUtil.firstCharUpperCase(identifier) + OneJsonUtil.firstCharUpperCase(outputIdentifier);
                    String type = outputDatadata.getDataType().getType();
            Object specs = outputDatadata.getDataType().getSpecs();
            switch (type) {
                case "struct":
                    if (specs instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) specs;
                        List<InOrOutputData> specsList = jsonArray.toJavaList(InOrOutputData.class);
                        List<FieldSpec> fieldSpecList = new ArrayList<>();
                        for (InOrOutputData data : specsList) {
                            if ("array".equals(data.getDataType().getType()) || "struct".equals(data.getDataType().getType())) {
                                throw new Exception("invalid dataType of identifier : " + data.getParamIdentifier());
                            }
                            String paramIdentifier = data.getParamIdentifier().replace("-", "");
                            AnnotationSpec annotation = AnnotationSpec.builder(JSONField.class).addMember("name", "$S", data.getParamIdentifier()).build();
                            fieldSpecList.add(FieldSpec.builder(getClassTypeByDataType(data.getDataType().getType()), paramIdentifier, Modifier.PRIVATE).addAnnotation(annotation).build());
                        }
                        buildStructDto(outputFileName, fieldSpecList);
                        ClassName classDto = ClassName.get("com.onenet.studio.acc.sdk.dto", outputFileName + STRUCT_DTO_SUFFIX);
                        AnnotationSpec annotation = AnnotationSpec.builder(JSONField.class).addMember("name", "$S", outputDatadata.getParamIdentifier()).build();
                        outputFileds.add(FieldSpec.builder(classDto, outputIdentifier).addAnnotation(annotation).build());
                    } else {
                        throw new Exception("invalid struct specs of identifier : " + identifier);
                    }
                    break;
                case "array":
                    ArrayType arrayType = JSON.parseObject(JSON.toJSONString(specs), ArrayType.class);
                    if ("array".equals(arrayType.getType())) {
                        throw new Exception("invalid arrayType : " + arrayType.getType());
                    } else if ("struct".equals(arrayType.getType())) {
                        Object arraySpecs = arrayType.getSpecs();
                        if (arraySpecs instanceof JSONArray) {
                            JSONArray jsonArray = (JSONArray) arraySpecs;
                            List<InOrOutputData> specsList = jsonArray.toJavaList(InOrOutputData.class);
                            List<FieldSpec> fields = new ArrayList<>();
                            for (InOrOutputData data : specsList) {
                                if ("array".equals(data.getDataType().getType()) || "struct".equals(data.getDataType().getType())) {
                                    throw new Exception("invalid dataType of identifier : " + data.getParamIdentifier());
                                }
                                String dataIdentifier = data.getParamIdentifier().replace("-", "");
                                AnnotationSpec annotation = AnnotationSpec.builder(JSONField.class).addMember("name", "$S", data.getParamIdentifier()).build();
                                fields.add(FieldSpec.builder(getClassTypeByDataType(data.getDataType().getType()), dataIdentifier, Modifier.PRIVATE).addAnnotation(annotation).build());
                            }
                            buildStructDto(outputFileName, fields);
                            ClassName classDto = ClassName.get("com.onenet.studio.acc.sdk.dto", outputFileName + STRUCT_DTO_SUFFIX);
                            AnnotationSpec annotation = AnnotationSpec.builder(JSONField.class).addMember("name", "$S", outputDatadata.getParamIdentifier()).build();
                            outputFileds.add(FieldSpec.builder(ArrayTypeName.of(classDto), outputIdentifier).addAnnotation(annotation).build());
                        } else {
                            throw new Exception("invalid struct data of identifier : " + identifier);
                        }
                    }
                    break;
                default:
                    AnnotationSpec annotation = AnnotationSpec.builder(JSONField.class).addMember("name", "$S", outputDatadata.getParamIdentifier()).build();
                    outputFileds.add(FieldSpec.builder(getClassTypeByDataType(type), outputIdentifier, Modifier.PRIVATE).addAnnotation(annotation).build());
                    break;
            }
        }
    }

    /**
     * 生成单个属性上报方法
     *
     * @author wjl
     * @date 2020/12/28
     * @param methodSpecBuilder 方法构建builder
     * @param identifier 属性标识符
     * @param originIdentifier 属性物模型原始标识符
     * @param index 当前参数在所有属性参数中的索引
     * @param totel 属性物模型的总数
     * @return 构建的单个属性上报方法
     **/
    private MethodSpec generateSinglePropUp(MethodSpec.Builder methodSpecBuilder
            , String identifier
            , String originIdentifier
            , int index
            , int totel) {
        methodSpecBuilder.addModifiers(Modifier.PUBLIC)
            .addJavadoc("单个属性功能点上报\n")
            .addJavadoc("\n")
            .addJavadoc(" @param $N 标识符为$N的属性功能点的值\n", identifier, originIdentifier)
            .addJavadoc(" @param timeout 超时时间，单位为毫秒")
            .addParameter(long.class, "timeout")
            .returns(int.class)
            .addException(Exception.class);
        StringBuilder builder = new StringBuilder();
        builder.append("return this.").append(PROPERTY_UPLOAD).append("(timeout, ");
        for (int i = 0; i < totel; i++) {
            if (i == index) {
                builder.append("$N, ");
            } else {
                builder.append("null, ");
            }
        }
        String newStr = builder.substring(0, builder.length() - 2) + ")";
        methodSpecBuilder.addStatement(newStr, identifier);
        return methodSpecBuilder.build();
    }

    /**
     * 生成单个属性上报方法
     *
     * @author wjl
     * @date 2020/12/28
     *
     * @param identifier 属性标识符
     * @param originIdentifier 属性物模型原始标识符
     * @param valClassName 属性的类型
     * @param index 当前参数在所有属性参数中的索引
     * @param totel 属性物模型的总数
     * @return 构建的单个属性上报方法
     **/
    public MethodSpec generateSinglePropUp(String identifier, String originIdentifier, TypeName valClassName, int index, int totel) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(identifier + OneJsonUtil.firstCharUpperCase(PROPERTY_UPLOAD))
                .addParameter(valClassName, identifier);
        return generateSinglePropUp(methodSpecBuilder, identifier, originIdentifier, index, totel);
    }

    /**
     * 生成单个属性上报方法
     *
     * @author wjl
     * @date 2020/12/28
     *
     * @param identifier 属性标识符
     * @param originIdentifier 属性物模型原始标识符
     * @param valType 属性的类型
     * @param index 当前参数在所有属性参数中的索引
     * @param totel 属性物模型的总数
     * @return 构建的单个属性上报方法
     **/
    public MethodSpec generateSinglePropUp(String identifier, String originIdentifier, Type valType, int index, int totel) {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(identifier + OneJsonUtil.firstCharUpperCase(PROPERTY_UPLOAD))
                .addParameter(valType, identifier);
        return generateSinglePropUp(methodSpecBuilder, identifier, originIdentifier, index, totel);
    }

    /**
     * 根据文件名读取物模型json文件
     *
     * @author wjl
     * @date 2020/12/28
     * @param path 物模型文件名称
     * @return 物模型内容
     **/
    private String readFileToString(String path) {
        StringBuilder contentBuilder = new StringBuilder();
        BufferedReader br = null;
        try {
            String filePath = System.getProperty("user.dir") + File.separator + File.separator + path;
            br = new BufferedReader(new FileReader(filePath));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return contentBuilder.toString();
    }
}
