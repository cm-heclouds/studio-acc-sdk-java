**南向接入sdk(Java)**
=================

本项目是中移物联网公司为方便Java开发者快速将设备接入OneNET Studio开发的SDK，关于
OneNET Studio请进入主站了解详情，如果要了解OneNET Studio 设备接入请参考文档中心。本项目包含了SDK源码和实践demo代码，实践demo列出了SDK的使用方式，便于开发者使用或者进行SDK二次开发


- [1.环境](#1环境)
- [2.项目结构](#2项目结构)
- [3.如何使用](#3如何使用)
    - [3.1.提前准备](#31提前准备)
        - [3.1.1.本地安装](#311本地安装)
    - [3.2.物模型文件配置和代码生成](#32物模型文件配置和代码生成)
    - [3.3.OpenApiExtention使用示例](#33OpenApiExtention使用示例)
    - [3.4.OpenApiExtention API](#34openapiextention-api)
        - [3.4.1.物模型功能总览](#341物模型功能总览)
        - [3.4.2.物模型功能点数据类型和生成的Java代码参数数据类型映射关系](#342物模型功能点数据类型和生成的Java代码参数数据类型映射关系)
        - [3.4.3.属性上报](#343属性上报)
        - [3.4.4.期望值获取和删除](#344期望值获取和删除)
        - [3.4.5.属性设置](#345属性设置)
        - [3.4.6.属性获取](#346属性获取)
        - [3.4.7.设备事件上报](#347设备事件上报)
        - [3.4.8.设备服务调用](#348设备服务调用)


# **1.环境**
JDK1.8及以上

# **2.项目结构**
```
studio-acc-sdk-java  
    |--studio-acc-sdk-process //sdk
    |--studio-acc-sdk-sample //使用示例
    |--pom
```

# **3.如何使用**

## **3.1.提前准备**

### **3.1.1.本地安装**
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

## **3.2.物模型文件配置和代码生成**

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

 ## **3.3.OpenApiExtention使用示例**

    ```java
    //创建OpenApiExtention对象即可
    OpenApiExtention extention = new OpenApiExtention(openApi);

## **3.4.OpenApiExtention API**

物模型上报和下发的数据格式见[OneNET官方文档](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/thing-model/protocol/OneJSON/OneJSON-introduce.html)

`注意：用户在使用物模型时需严格执行物模型定义填写的数据类型限制。如int32、int64类型的数据，在物模型定义时会填写定义取值范围、步长等内容，则填写的数据应满足填写的取值范围和步长等要求，否则会上报失败`

### **3.4.1.物模型功能总览**

| API功能 | 调用方法 | 返回值 | 备注 |
| :-----: | :----: | :----: | :----: |
| 多个属性上报 | propertyUpload | 返回类型：int，0:上报成功,1:上报失败 | 参数为需要上报的设备属性，参数为null的数据项不参与上报 |
| 单个属性上报 | *PropertyUpload | 返回类型：int，0:上报成功,1:上报失败 | *表示功能点标识符，如有一个identifier为test的功能点，则方法名为testPropertyUpload |
| 期望值获取 | propertyDesiredGet | 返回类型：com.alibaba.fastjson.JSONObject类型 | 参数为想要获取的功能点的标识符数组 |
| 期望值删除 | propertyDesiredDel | 返回类型：int，0:删除成功,1:删除失败 | 参数为要删除的功能点的标识符（key）和版本（value）的Map，版本可在期望值获取接口上查看 |
| 属性设置 | propertySet | 无返回值，该方法为订阅属性设置topic | 有属性设置的命令时会调用参数中用户自定义的回调函数，用户处理完业务逻辑后`需要调用propertySetReply`方法回复平台设置结果 |
| 属性获取 | propertyGet | 无返回值，该方法为订阅属性获取topic | 有属性获取的命令时会调用参数中用户自定义的回调函数，用户处理完业务逻辑后`需要调用propertyGetReply`方法回复平台获取结果 |
| 设备事件上报 | eventUpload | 返回类型：int，0:上报成功，1:上报失败 | 参数为需要上报的设备事件，参数为null的数据项不参与上报 |
| 设备服务调用 | *ServiceInvoke | 无返回值，该方法为订阅服务调用topic | * 表示功能点标识符，如有一个identifier为test的标识符，则方法名为testServiceInvoke,表示test的服务订阅，当有test的服务调用命令时会调用参数中用户自定义的回调函数，用户处理完业务逻辑后`需要调用*ServiceInvokeReply`方法回复平台相应的服务调用结果，同理*表示标识符，如标识符为test，则回复的方法为testServiceInvokeReply |

### **3.4.2.物模型功能点数据类型和生成的Java代码参数数据类型映射关系**

<table>
    <tr>
        <td>功能点数据类型</td>
        <td>java数据类型</td>
    </tr>
    <tr>
        <td>int32</td>
        <td>Integer</td>
    </tr>
    <tr>
        <td>int64</td>
        <td>Long</td>
    </tr>
    <tr>
        <td>float</td>
        <td>Float</td>
    </tr>
    <tr>
        <td>double</td>
        <td>Double</td>
    </tr>
    <tr>
        <td>enum</td>
        <td>Integer</td>
    </tr>
    <tr>
        <td>bool</td>
        <td>Boolean</td>
    </tr>
    <tr>
        <td>string</td>
        <td>String</td>
    </tr>
    <tr>
        <td>struct</td>
        <td>Object(在com.onenet.studio.acc.sdk.dto中生成具体的DTO类)</td>
    </tr>
    <tr>
        <td>bitMap</td>
        <td>Long</td>
    </tr>
    <tr>
        <td>date</td>
        <td>Long</td>
    </tr>
    <tr>
        <td>array</td>
        <td>Object[](在com.onenet.studio.acc.sdk.dto中生成具体的DTO类)</td>
    </tr>
</table>

### **3.4.3.属性上报**

设备主动上报物模型中属性类型的功能点，API中提供了单个属性上报和多个属性上报两种方式。

- 单个属性上报的方法名为`标识符+PropertyUpload`，如标识符为test，则方法名为testPropertyUpload(timeout, devVal)。

    - 请求参数：
        - timeout为上报超时时间（单位为毫秒）
        - devVal参数为设备的属性值，具体数据格式依对应功能点的类型决定

    - 返回值：

        - 0：上报成功；1：上报失败

- propertyUpload(timeout, devVal)

    - 请求参数：
        - timeout为上报超时时间（单位为毫秒）
        - devVal参数与属性的个数相同，如用户有int32类型的标识符为test1功能点和double类型的标识符为test2功能点，则对应的方法为`propertyUpload(long timeout, Integer test1, Double test2)`

    - 返回值：
        - 0：上报成功；1：上报失败

### **3.4.4.期望值获取和删除**

期望值为用户在平台设置的设备属性的预期值，在用户设置成功后设备可从平台获取或删除对应的属性期望值。

- propertyGet(String[] identifiers, long timeout)。
    
    - 请求参数
        - identfiers为想要获取期望值的属性的标识符数组
        - timeout为上报超时时间（单位为毫秒）
    
    - 返回值
        - JSONObject对象，数据格式见[物模型格式文档](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/thing-model/protocol/OneJSON/desired.html)

- 期望值删除方法名`propertyDesiredDel(long timeout, Map identifiers)`

    - 请求参数
        - timeout为上报超时时间（单位为毫秒）
        - identifiers是属性标识符为key，期望值版本为value的Map，期望值版本可在`期望值获取`接口中可知,

    - 返回值
        - 0：删除成功；1：删除失败

### **3.4.5.属性设置**

该功能为平台向设备下发命令修改设备属性值。

- propertySet(OpenApiCallback callback)
    
    - 请求参数
        - callback 用户自定义回调函数，回调函数中参数为oneJson字符串，数据格式见[官方文档](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/thing-model/protocol/OneJSON/property&event.html)，用户实现业务逻辑后需调用`propertySetReply(String messageId, Integer code, String msg)`方法回复平台设置结果

### **3.4.6.属性获取**

该功能为用户通过平台向设备下发命令获取属性（同步调用）

- propertyGet(OpenApiCallback callback)

    - 请求参数
        - callback 用户自定义回调函数，回调函数中参数为oneJson字符串，数据格式见[官方文档](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/thing-model/protocol/OneJSON/property&event.html),用户实现业务逻辑后需调用`propertyGetReply(String messageId, Integer code, String msg, Map data)`方法回复平台获取结果。
            - messageId 消息id，获取的oneJson中的id字段值
            - code 返回状态码
            - msg 返回消息
            - data 返回属性数据

### **3.4.7.设备事件上报**

该功能为设备上报事件类型功能点

- eventUpload(timeout, devEventVal)
    
    - 请求参数
        - timeout 请求超时时间
        - devEventVal 设备事件的值，该参数个数与物模型定义的事件类型功能点个数相同，每一个事件类型都会生成一个DTO类，存放于生成的com.onenet.studio.acc.sdk.dto包下，若不上报某个事件，则对应的参数设null即可。如有标识符为test1和test2的两个事件类型功能点，则上报事件的方法为eventUpload(long time, Test1StructDTO test1, Test2StructDTO test2)，此时只上报test2的事件，不上报test1，则可如此调用：eventUpload(timeout, null, test2)。

    - 返回结果
        - 0：上报成功；1：上报失败

### **3.4.8.设备服务调用**

该功能为用户通过平台向设备下发服务调用的命令

- 服务调用方法名为`标识符+ServiceInvoke(callback)`，如标识符为test，则方面名为testServiceInvoke(callback)

    - 请求参数 用户自定义回调函数，回调函数中参数为oneJson字符串，数据格式见[官方文档](https://open.iot.10086.cn/doc/iot_platform/book/device-connect&manager/thing-model/protocol/OneJSON/service.html)。用户实现业务逻辑后续调用`标识符+ServiceInvokeReply(messageId, code, msg, serviceOutputData)`方法回复平台服务调用结果。如标识符为test，则回复平台的方法为：testServiceInvokeReply(String messageId, Integer code, String msg, Object serviceOutputData)。
        - messageId 消息id，获取的oneJson中的id字段值
        - code 返回状态码
        - msg 返回消息
        - serviceOutputData 物模型服务类型功能点定义中的输出参数的DTO类，如服务功能点的标识符为test，则该参数的类型为TestStructDTO，存在于生成的com.onenet.studio.acc.sdk.dto包下。