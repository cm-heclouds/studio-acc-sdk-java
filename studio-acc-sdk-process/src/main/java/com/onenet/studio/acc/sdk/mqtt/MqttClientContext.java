package com.onenet.studio.acc.sdk.mqtt;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.onenet.studio.acc.sdk.interfaces.OpenApiCallback;
import com.onenet.studio.acc.sdk.util.EncryptUtil;
import com.onenet.studio.acc.sdk.util.SslUtil;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author: fanhaiqiu
 * @date: 2020/12/22
 */
public class MqttClientContext {

    /**
     * 固定版本
     */
    private static final String version = "2018-10-31";

    private final List<String> topics = new CopyOnWriteArrayList<>();

    private final Map<String, OpenApiCallback> callbackMap = new HashMap<>();

    /**
     * 等待对象
     */
    private final Map<String, Object> awaitObjects = new ConcurrentHashMap<>();

    /**
     * 数据集合
     */
    private final Cache<String, JSONObject> dataCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();
    /**
     * mqtt 地址
     */
    private String url;

    /**
     * 设备唯一标识
     */
    private String devKey;

    /**
     * 产品
     */
    private String productId;

    /**
     * 是否支持ssl
     */
    private byte[] caCrtFile;

    /**
     * 产品accessKey
     */
    private String accessKey;

    /**
     * token 过期时间 单位毫秒
     */
    private long expireTime;

    /**
     * 回调
     */
    private OnMessageCallback callback;

    /**
     * mqtt客户端
     */
    private MqttClient mqttClient;

    /**
     * 签名加密方法
     */
    private String signatureMethod;

    /**
     * topic集合
     */
    private Topic topic;


    public static MqttClientContext create() {
        return new MqttClientContext();
    }

    /**
     * 初始化
     *
     * @return
     * @throws Exception
     */
    public void init() throws Exception {
        this.topic = new Topic(this.productId, this.devKey);
        MqttClient client = new MqttClient(this.url, this.devKey);
        MqttConnectOptions connOpts = createConnOpts();
        client.connect(connOpts);
        this.createCallback();
        client.setCallback(this.callback);
        this.mqttClient = client;
    }


    public MqttClientContext url(String url) {
        this.url = url;
        return this;
    }

    public MqttClientContext devKey(String devKey) {
        this.devKey = devKey;
        return this;
    }

    public MqttClientContext productId(String productId) {
        this.productId = productId;
        return this;
    }

    public MqttClientContext caCrtFile(byte[] caCrtFile) {
        this.caCrtFile = caCrtFile;
        return this;
    }

    public MqttClientContext accessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public MqttClientContext expireTime(long expireTime) {
        this.expireTime = expireTime;
        return this;
    }

    public MqttClientContext signatureMethod(String signatureMethod) {
        this.signatureMethod = signatureMethod;
        return this;
    }

    public MqttConnectOptions createConnOpts() throws Exception {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        connOpts.setUserName(this.productId);
        connOpts.setHttpsHostnameVerificationEnabled(false);
        StringBuilder sb = new StringBuilder();
        String resourceName = sb.append("products/").append(this.productId).append("/devices/").append(devKey).toString();
        String expire = String.valueOf(System.currentTimeMillis() + expireTime);
        String token = EncryptUtil.assembleToken(version, resourceName, expire, this.signatureMethod, this.accessKey);
        connOpts.setPassword(token.toCharArray());
        if (caCrtFile != null && caCrtFile.length != 0) {
            connOpts.setSocketFactory(SslUtil.getSocketFactory(this.caCrtFile));
        }
        return connOpts;
    }


    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public List<String> getTopics() {
        return topics;
    }

    public Map<String, OpenApiCallback> getCallbackMap() {
        return callbackMap;
    }

    /**
     * 属性消息上报发布
     *
     * @param oneJson
     */
    public void propertyPostPublish(String oneJson) throws Exception {
        this.publish(topic.getPropertyPostTopic(), oneJson);
    }

    /**
     * 属性上报订阅
     *
     * @throws Exception
     */
    public void propertyPostReplySubscribe() throws Exception {
        this.subscribe(topic.getPropertyPostReplyTopic());
    }

    /**
     * 属性设置
     *
     * @throws Exception
     */
    public void propertySetSubscribe(OpenApiCallback callback) throws Exception {
        callbackMap.put(topic.getPropertySetTopic(), callback);
        this.subscribe(topic.getPropertySetTopic());
    }

    /**
     * 属性设置返回
     *
     * @throws Exception
     */
    public void propertySetReplyPublish(String oneJson) throws Exception {
        this.publish(topic.getPropertySetReplyTopic(), oneJson);
    }

    /**
     * 获取期望值发布
     *
     * @param oneJson
     */
    public void desiredGetPublish(String oneJson) throws Exception {
        this.publish(topic.getDesiredGetTopic(), oneJson);
    }

    /**
     * 获取期望值返回订阅
     *
     * @return oneJson
     */
    public void desiredGetReplySubscribe() throws Exception {
        this.subscribe(topic.getDesiredGetReplyTopic());
    }

    /**
     * 删除期望值发布
     *
     * @param oneJson
     */
    public void desiredDelPublish(String oneJson) throws Exception {
        this.publish(topic.getDesiredDelTopic(), oneJson);
    }

    /**
     * 删除期望值返回订阅
     *
     * @return oneJson
     */
    public void desiredDelReplySubscribe() throws Exception {
        this.subscribe(topic.getDesiredDelReplyTopic());
    }

    /**
     * 属性获取返回发布
     *
     * @param oneJson
     */
    public void propertyGetPublish(String oneJson) throws Exception {
        this.publish(topic.getPropertyGetReplyTopic(), oneJson);
    }

    /**
     * 属性获取订阅
     */
    public void propertyGetSubscribe(OpenApiCallback callback) throws Exception {
        callbackMap.put(topic.getPropertyGetTopic(), callback);
        this.subscribe(topic.getPropertyGetTopic());
    }

    /**
     * 事件上报
     *
     * @param oneJson
     * @throws Exception
     */
    public void eventPostPublish(String oneJson) throws Exception {
        this.publish(topic.getEventPostTopic(), oneJson);
    }

    /**
     * 事件上报回复订阅
     *
     * @throws Exception
     */
    public void eventPostReplySubscribe() throws Exception {
        this.subscribe(topic.getEventPostReplyTopic());
    }

    /**
     * 服务调用发布返回消息
     *
     * @param identifier
     * @param oneJson
     * @throws Exception
     */
    public void serviceInvokePublish(String identifier, String oneJson) throws Exception {
        String topic = String.format(Topic.SERVICE_INVOKE_REPLY_FMT, productId, devKey, identifier);
        this.publish(topic, oneJson);
    }

    /**
     * 服务调用订阅
     *
     * @param identifier
     * @param callback
     */
    public void serviceInvokeSubscribe(String identifier, OpenApiCallback callback) throws Exception {
        String topic = String.format(Topic.SERVICE_INVOKE_FMT, productId, devKey, identifier);
        callbackMap.put(topic, callback);
        this.subscribe(topic);
    }

    /**
     * 增加等待对象
     *
     * @param id
     * @param o
     */
    public void putAwaitObject(String id, Object o) {
        this.awaitObjects.put(id, o);
    }

    /**
     * 获取等待对象
     *
     * @param id
     */
    public Object getAwaitObject(String id) {
        return this.awaitObjects.get(id);
    }

    /**
     * 移除等待对象
     *
     * @param id
     */
    public void removeAwaitObject(String id) {
        this.awaitObjects.remove(id);
    }

    /**
     * 获取返回消息
     *
     * @param id
     * @return
     */
    public JSONObject getReply(String id) {
        JSONObject reply = dataCache.getIfPresent(id);
        if (Objects.nonNull(reply)) {
            dataCache.invalidate(id);
        }
        return reply;
    }

    /**
     * 存放返回数据
     *
     * @param id
     * @param reply
     */
    public void putReply(String id, JSONObject reply) {
        dataCache.put(id, reply);
    }

    /**
     * 消息发布
     *
     * @param topic
     * @param message
     * @throws Exception
     */
    public void publish(String topic, String message) throws Exception {
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        this.mqttClient.publish(topic, mqttMessage);
    }

    /**
     * 消息订阅
     *
     * @param topic
     */
    private void subscribe(String topic) throws Exception {
        if (this.topics.contains(topic)) {
            return;
        }
        this.mqttClient.subscribe(topic);
        this.topics.add(topic);
    }

    private MqttClientContext createCallback() {
        OnMessageCallback callback = new OnMessageCallback();
        callback.setContext(this);
        this.callback = callback;
        return this;
    }


    private class Topic {
        public static final String SERVICE_INVOKE_FMT = "$sys/%s/%s/thing/service/%s/invoke";
        public static final String SERVICE_INVOKE_REPLY_FMT = "$sys/%s/%s/thing/service/%s/invoke_reply";

        private static final String PROPERTY_POST_FMT = "$sys/%s/%s/thing/property/post";
        private static final String PROPERTY_POST_REPLY_FMT = "$sys/%s/%s/thing/property/post/reply";

        private static final String PROPERTY_SET_FMT = "$sys/%s/%s/thing/property/set";
        private static final String PROPERTY_SET_REPLY_FMT = "$sys/%s/%s/thing/property/set_reply";

        private static final String DESIRED_GET_FMT = "$sys/%s/%s/thing/property/desired/get";
        private static final String DESIRED_GET_REPLY_FMT = "$sys/%s/%s/thing/property/desired/get/reply";

        private static final String DESIRED_DEL_FMT = "$sys/%s/%s/thing/property/desired/delete";
        private static final String DESIRED_DEL_REPLY_FMT = "$sys/%s/%s/thing/property/desired/delete/reply";

        private static final String PROPERTY_GET_FMT = "$sys/%s/%s/thing/property/get";
        private static final String PROPERTY_GET_REPLY_FMT = "$sys/%s/%s/thing/property/get_reply";

        private static final String EVENT_POST_FMT = "$sys/%s/%s/thing/event/post";
        private static final String EVENT_POST_REPLY_FMT = "$sys/%s/%s/thing/event/post/reply";

        /**
         * 属性上报
         */
        private String propertyPostTopic;

        /**
         * 属性上报回复
         */
        private String propertyPostReplyTopic;

        /**
         * 属性设置
         */
        private String propertySetTopic;

        /**
         * 属性上报回复
         */
        private String propertySetReplyTopic;

        /**
         * 期望值获取
         */
        private String desiredGetTopic;

        /**
         * 期望值获取发布
         */
        private String desiredGetReplyTopic;

        /**
         * 期望值删除
         */
        private String desiredDelTopic;

        /**
         * 期望值删除发布
         */
        private String desiredDelReplyTopic;

        /**
         * 属性获取
         */
        private String propertyGetTopic;

        /**
         * 属性获取返回
         */
        private String propertyGetReplyTopic;

        /**
         * 事件上报
         */
        private String eventPostTopic;

        /**
         * 事件上报回复
         */
        private String eventPostReplyTopic;


        Topic(String productId, String devKey) {
            this.propertyPostTopic = String.format(PROPERTY_POST_FMT, productId, devKey);
            this.propertyPostReplyTopic = String.format(PROPERTY_POST_REPLY_FMT, productId, devKey);
            this.propertySetTopic = String.format(PROPERTY_SET_FMT, productId, devKey);
            this.propertySetReplyTopic = String.format(PROPERTY_SET_REPLY_FMT, productId, devKey);
            this.desiredGetTopic = String.format(DESIRED_GET_FMT, productId, devKey);
            this.desiredGetReplyTopic = String.format(DESIRED_GET_REPLY_FMT, productId, devKey);
            this.desiredDelTopic = String.format(DESIRED_DEL_FMT, productId, devKey);
            this.desiredDelReplyTopic = String.format(DESIRED_DEL_REPLY_FMT, productId, devKey);
            this.propertyGetTopic = String.format(PROPERTY_GET_FMT, productId, devKey);
            this.propertyGetReplyTopic = String.format(PROPERTY_GET_REPLY_FMT, productId, devKey);
            this.eventPostTopic = String.format(EVENT_POST_FMT, productId, devKey);
            this.eventPostReplyTopic = String.format(EVENT_POST_REPLY_FMT, productId, devKey);
        }

        public String getPropertyPostTopic() {
            return propertyPostTopic;
        }

        public String getPropertyPostReplyTopic() {
            return propertyPostReplyTopic;
        }

        public String getPropertySetTopic() {
            return propertySetTopic;
        }

        public String getPropertySetReplyTopic() {
            return propertySetReplyTopic;
        }

        public String getDesiredGetTopic() {
            return desiredGetTopic;
        }

        public String getDesiredGetReplyTopic() {
            return desiredGetReplyTopic;
        }

        public String getDesiredDelTopic() {
            return desiredDelTopic;
        }

        public String getDesiredDelReplyTopic() {
            return desiredDelReplyTopic;
        }

        public String getPropertyGetTopic() {
            return propertyGetTopic;
        }

        public String getPropertyGetReplyTopic() {
            return propertyGetReplyTopic;
        }

        public String getEventPostTopic() {
            return eventPostTopic;
        }

        public String getEventPostReplyTopic() {
            return eventPostReplyTopic;
        }
    }
}
