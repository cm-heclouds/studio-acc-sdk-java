package com.onenet.studio.acc.sdk.util;

import com.alibaba.fastjson.JSON;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.*;

/**
 * 物模型工具类
 *
 * @author wjl
 * @date 2020-12-23
 */
public class OneJsonUtil {

    /**
     * 构建基础oneJson代码
     *
     * @author wjl
     * @date 2020/12/24
     * @param builder 方法构建builder
     * @return void
     **/
    public static void getBaseOneJson(MethodSpec.Builder builder) {
        builder.addStatement("$T<String, Object> oneJson = new $T<>()", Map.class, HashMap.class)
                .addStatement("long now = System.currentTimeMillis()")
                .addStatement("String id = String.valueOf(now)")
                .addStatement("oneJson.put(ONEJSON_ID_KEY, id)")
                .addStatement("oneJson.put(ONEJSON_VERSION_KEY, ONEJSON_VERSION_VAL)");

    }

    /**
     * 添加参数和json序列化oneJson
     *
     * @author wjl
     * @date 2020/12/24
     * @param builder 方法构建builder
     * @return void
     **/
    public static void finishParamsOneJson(MethodSpec.Builder builder) {
        builder.addStatement("oneJson.put(\"params\", params)");
        builder.addStatement("String oneJsonStr = $T.toJSONString(oneJson)", JSON.class);
    }

    /**
     * 添加参数和json序列化oneJson
     *
     * @author wjl
     * @date 2020/12/24
     * @param builder 方法构建builder
     * @return void
     **/
    public static void finishDataOneJson(MethodSpec.Builder builder) {
        builder.addStatement("String oneJsonStr = $T.toJSONString(map)", JSON.class);
    }

    public static void propertyUpOneJson(MethodSpec.Builder builder, Map<String, String> params, Map<String, String> identifierMap) {
        getBaseOneJson(builder);
        builder.addStatement("$T<String, Object> params = new $T<>()", Map.class, HashMap.class);
        Set<String> keys = params.keySet();
        int i = 1;
        for (String key : keys) {
            if ("timeout".equals(key)) {
                return;
            }
            builder.beginControlFlow("if (!$T.isNull($N))", Objects.class, params.get(key));
            builder.addStatement("    $T<String, Object> val" + i +" = new $T<>()", Map.class, HashMap.class);
            builder.addStatement("    val" + i + ".put(\"value\", $N)", params.get(key))
                    .addStatement("    val" + i +".put(\"time\", now)")
                    .addStatement("    params.put($S, val" + i +")", identifierMap.get(key));
            builder.endControlFlow();
            i++;
        }
        finishParamsOneJson(builder);
    }

    public static void desiredGet(MethodSpec.Builder builder, String paramName) {
        getBaseOneJson(builder);
        builder.addStatement("Object params = JSON.toJSON($N)", paramName);
        finishParamsOneJson(builder);
    }

    public static void desiredDel(MethodSpec.Builder builder, String paramsName) {
        getBaseOneJson(builder);
        builder.addStatement("$T<String, Object> params = new $T<>()", Map.class, HashMap.class)
                .addStatement("$T<String> keys = $N.keySet()", Set.class, paramsName)
                .beginControlFlow("for (String identifier : keys)")
                .addStatement("$T<String, Object> version = new $T<>()", Map.class, HashMap.class)
                .addStatement("version.put(\"version\", $N.get(identifier))", paramsName)
                .addStatement("params.put(identifier, version)")
                .endControlFlow();
        finishParamsOneJson(builder);
    }

    public static void getBaseReplyOneJson(MethodSpec.Builder builder) {
        builder.addStatement("$T map = new $T()", Map.class, HashMap.class)
                .addStatement("map.put(\"id\", messageId)")
                .addStatement("map.put(\"code\", code)")
                .addStatement("map.put(\"msg\", msg)");
    }

    public static void eventUpOneJson(MethodSpec.Builder builder, Map<String, String> params, Map<String, String> identifierMap) {
        getBaseOneJson(builder);
        builder.addStatement("$T<String, Object> params = new $T<>()", Map.class, HashMap.class);
        Set<String> keys = params.keySet();
        int i = 1;
        for (String key : keys) {
            if ("timeout".equals(key)) {
                return;
            }
            builder.beginControlFlow("if (!$T.isNull($N))", Objects.class, key);
            builder.addStatement("    $T<String, Object> val" + i +" = new $T<>()", Map.class, HashMap.class);
            builder.addStatement("    val" + i + ".put(\"value\", $N)", params.get(key))
                    .addStatement("    val" + i +".put(\"time\", now)")
                    .addStatement("    params.put($S, val" + i +")", identifierMap.get(key));
            builder.endControlFlow();
            i++;
        }
        finishParamsOneJson(builder);
    }

    public static void serviceInvokeReply(MethodSpec.Builder builder, String val) {
        getBaseReplyOneJson(builder);
        builder.addStatement("map.put(\"data\", $N)", val);
        finishDataOneJson(builder);
    }

    public static String firstCharUpperCase(String str) {
        char[] cs = str.toCharArray();
        if (97 > cs[0] || 122 < cs[0]) {
            return str;
        }
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    public static String firstCharLowerCase(String str) {
        char[] cs = str.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }
}
