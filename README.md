## 南向接入sdk(Java)
本项目是中移物联网公司为方便Java开发者快速将设备接入OneNET Studio开发的SDK，关于
OneNET Studio请进入主站了解详情，如果要了解OneNET Studio 设备接入请参考文档中心。本项目包含了SDK源码和实践demo代码，实践demo列出了SDK的使用方式，便于开发者使用或者进行二次开发

## 环境
JDK1.8及以上

## 项目结构
```
studio-acc-sdk-java  
    |--studio-acc-sdk-process //sdk
    |--studio-acc-sdk-sample //使用示例
    |--pom
```

## 如何使用

### 提前准备 

#### 本地安装
+ 获取本项目
```
    git clone git@github.com:cm-heclouds/studio-acc-sdk-java.git
```
+ 安装至本地仓库
```
    mvn clean package install -Dmaven.test.skip=true
```
+ 项目使用引入
```
    <dependency>
        <groupId>com.onenet.studio</groupId>
        <artifactId>acc-sdk-process</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
```
这样就完成sdk的引入，具体使用请参考studio-acc-sdk-sample项目

### 物模型文件配置和代码生成

1. 开发者需在**OneNET Studio控制台 — 设备接入与管理 — 产品管理 — 产品详情 — 查看和导出物模型 — 导出物模型**中导出物模型的json配置文件，放置于Java工程的根目录下。
2. 在任一类上添加@ThingsModelConfiguration注解，填入物模型配置文件的相对路径，例如配置文件在src/main/resouces目录下，则注解为：

    ```java
    @ThingsModelConfiguration("src/main/resources/things_model_configuration.json")
    public class Application {

    }

3. 对maven项目进行compile操作。编译完成后生成的class文件夹中（IDEA的生成路径为`target/generated-sources/annotations/`）会生成`com.onenet.studio.acc.sdk.OpenApiExtention.java`文件和`com.onenet.studio.acc.sdk.dto`包下多个DTO类文件（该包和文件只在有特定物模型类型时才会生成，故非必需文件）。用户可使用`OpenApiExtention.java`中的API进行物模型的使用。

注意：在编译或运行时可能会报如下错误，此时可在IDE中关闭注解处理器。

   ```java
   Error:java: 服务配置文件不正确, 或构造处理程序对象javax.annotation.processing.Processor: Provider ***
   ```

   IDEA:
   
   File -> Settings -> Build,Execution,Deployment -> Compiler -> Annotation Processor -> 取消勾选`Enable annotation processing`

 ### `OpenApiExtention`使用示例
    //创建OpenApiExtention对象即可
    OpenApiExtention extention = new OpenApiExtention(openApi);

### `OpenApiExtention` API使用

物模型上报和下发的数据格式见[官方文档](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/thing-model/protocol/OneJSON/OneJSON-introduce.html)

| API功能 | 调用方法 | 返回值 | 备注 |
| :-----: | :----: | :----: | :----: |
| 多个属性上报 | propertyUpload | 返回类型：int，0:上报成功,1:上报失败 | 参数为需要上报的设备属性，参数为null的数据项不参与上报 |
| 单个属性上报 | *PropertyUpload | 返回类型：int，0:上报成功,1:上报失败 | *表示功能点标识符，如有一个identifier为test的标识符，则方法名为testPropertyUpload |
| 期望值获取 | propertyDesiredGet | 返回类型：com.alibaba.fastjson.JSONObject类型 | 参数为想要获取的功能点的标识符数组 |
| 期望值删除 | propertyDesiredDel | 返回类型：int，0:删除成功,1:删除失败 | 参数为要删除的功能点的标识符（key）和版本（value）的Map，版本可在期望值获取接口上查看 |
| 属性设置 | propertySet | 无返回值，该方法为订阅属性设置topic | 有属性设置的命令时会调用参数中用户自定义的回调函数，用户处理完业务逻辑后`需要调用propertySetReply`方法回复平台设置结果 |
| 属性获取 | propertyGet | 无返回值，该方法为订阅属性获取topic | 有属性获取的命令时会调用参数中用户自定义的回调函数，用户处理完业务逻辑后`需要调用propertyGetReply`方法回复平台获取结果 |
| 设备事件上报 | eventUpload | 返回类型：int，0:上报成功，1:上报失败 | 参数为需要上报的设备事件，参数为null的数据项不参与上报 |
| 设备服务调用 | *ServiceInvoke | 无返回值，该方法为订阅服务调用topic | * 表示功能点标识符，如有一个identifier为test的标识符，则方法名为testServiceInvoke,表示test的服务订阅，当有test的服务调用命令时会调用参数中用户自定义的回调函数，用户处理完业务逻辑后`需要调用*ServiceInvokeReply`方法回复平台相应的服务调用结果，同理*表示标识符，如标识符为test，则回复的方法为testServiceInvokeReply |