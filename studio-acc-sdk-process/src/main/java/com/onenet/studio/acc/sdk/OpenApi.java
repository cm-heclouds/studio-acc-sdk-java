package com.onenet.studio.acc.sdk;

import com.alibaba.fastjson.JSONObject;
import com.onenet.studio.acc.sdk.interfaces.OpenApiCallback;
import com.onenet.studio.acc.sdk.mqtt.MqttClientContext;
import com.onenet.studio.acc.sdk.util.EncryptUtil;

import java.util.Objects;

/**
 * @author: fanhaiqiu
 * @date: 2020/12/22
 */
public class OpenApi {

    private static final Integer SUC_CODE = 0;
    private static final Integer FAIL_CODE = 1;
    private static final String KEY_CODE = "code";
    private static final Integer SUCCESS = 200;

    private MqttClientContext mqttClientContext;

    public void setMqttClientContext(MqttClientContext mqttClientContext) {
        this.mqttClientContext = mqttClientContext;
    }

    /**
     * 属性上班
     *
     * @param id      消息id
     * @param oneJson
     * @param timeout
     * @return 0-success;1-fail
     * @throws Exception
     */
    public int propertyPost(String id, String oneJson, long timeout) throws Exception {
        this.mqttClientContext.propertyPostReplySubscribe();
        Object awaitObject = new Object();
        mqttClientContext.putAwaitObject(id, awaitObject);
        synchronized (awaitObject) {
            this.mqttClientContext.propertyPostPublish(oneJson);
            awaitObject.wait(timeout);
        }
        mqttClientContext.removeAwaitObject(id);
        JSONObject reply = mqttClientContext.getReply(id);
        if (Objects.nonNull(reply) && isSuccess(reply)) {
            return SUC_CODE;
        }
        return FAIL_CODE;
    }

    /**
     * 属性设置-subscribe
     *
     * @return
     */
    public void propertySetSubscribe(OpenApiCallback callback) throws Exception {
        this.mqttClientContext.propertySetSubscribe(callback);
    }

    /**
     * 属性设置-publish
     *
     * @return
     */
    public void propertySetPublish(String oneJson) throws Exception {
        this.mqttClientContext.propertySetReplyPublish(oneJson);
    }


    /**
     * 获取属性
     *
     * @param id
     * @param oneJson
     * @param timeout
     * @return if return null indicate fail, normally return oneJson(jsonObject)
     * @throws Exception
     */
    public JSONObject desiredGet(String id, String oneJson, long timeout) throws Exception {
        this.mqttClientContext.desiredGetReplySubscribe();
        Object awaitObject = new Object();
        mqttClientContext.putAwaitObject(id, awaitObject);
        synchronized (awaitObject) {
            this.mqttClientContext.desiredGetPublish(oneJson);
            awaitObject.wait(timeout);
        }
        mqttClientContext.removeAwaitObject(id);
        return mqttClientContext.getReply(id);
    }

    /**
     * 删除期望值
     *
     * @param id
     * @param oneJson
     * @param timeout
     * @return 0-success;1-fail
     * @throws Exception
     */
    public int desiredDel(String id, String oneJson, long timeout) throws Exception {
        this.mqttClientContext.desiredDelReplySubscribe();
        Object awaitObject = new Object();
        mqttClientContext.putAwaitObject(id, awaitObject);
        synchronized (awaitObject) {
            this.mqttClientContext.desiredDelPublish(oneJson);
            awaitObject.wait(timeout);
        }
        mqttClientContext.removeAwaitObject(id);
        Object reply = mqttClientContext.getReply(id);
        if (Objects.nonNull(reply) && isSuccess(reply)) {
            return SUC_CODE;
        }
        return FAIL_CODE;
    }

    /**
     * 属性查询-subscribe
     *
     * @return
     */
    public void propertyGetSubscribe(OpenApiCallback callback) throws Exception {
        this.mqttClientContext.propertyGetSubscribe(callback);
    }

    /**
     * 属性查询-publish
     *
     * @return
     */
    public void propertyGetPublish(String oneJson) throws Exception {
        this.mqttClientContext.propertyGetPublish(oneJson);
    }

    /**
     * 事件上报
     *
     * @param id
     * @param oneJson
     * @param timeout
     * @return
     * @throws Exception
     */
    public int eventPost(String id, String oneJson, long timeout) throws Exception {
        this.mqttClientContext.eventPostReplySubscribe();
        Object awaitObject = new Object();
        mqttClientContext.putAwaitObject(id, awaitObject);
        synchronized (awaitObject) {
            this.mqttClientContext.eventPostPublish(oneJson);
            awaitObject.wait(timeout);
        }
        mqttClientContext.removeAwaitObject(id);
        Object reply = mqttClientContext.getReply(id);
        if (Objects.nonNull(reply) && isSuccess(reply)) {
            return SUC_CODE;
        }
        return FAIL_CODE;
    }

    /**
     * 属性查询-subscribe
     *
     * @return
     */
    public void serviceInvokeSubscribe(String identifier, OpenApiCallback callback) throws Exception {
        this.mqttClientContext.serviceInvokeSubscribe(identifier, callback);
    }

    /**
     * 属性查询-publish
     *
     * @return
     */
    public void serviceInvokePublish(String identifier, String oneJson) throws Exception {
        this.mqttClientContext.serviceInvokePublish(identifier, oneJson);
    }


    public static final class Builder {
        /**
         * mqtt 地址
         */
        private String url;

        /**
         * 产品
         */
        private String productId;

        /**
         * 设备唯一标识
         */
        private String devKey;

        /**
         * 产品accessKey
         */
        private String accessKey;

        /**
         * ssl文件
         */
        private byte[] caCrtFile;

        /**
         * token 过期时间 单位毫秒，默认一天过期
         */
        private long expireTime = 1000 * 60 * 60 * 24;

        /**
         * 签名加密方法
         */
        private String signatureMethod = EncryptUtil.SignatureMethod.SHA1.name().toLowerCase();

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder devKey(String devKey) {
            this.devKey = devKey;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder caCrtFile(byte[] caCrtFile) {
            this.caCrtFile = caCrtFile;
            return this;
        }

        public Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public Builder expireTime(long expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public Builder signatureMethod(String signatureMethod) {
            this.signatureMethod = signatureMethod;
            return this;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public OpenApi build() throws Exception {
            this.check();
            OpenApi openApi = new OpenApi();
            MqttClientContext mqttClientContext = MqttClientContext.create()
                    .url(this.url)
                    .productId(this.productId)
                    .devKey(this.devKey)
                    .accessKey(this.accessKey)
                    .caCrtFile(this.caCrtFile)
                    .expireTime(this.expireTime)
                    .signatureMethod(this.signatureMethod);
            mqttClientContext.init();
            openApi.setMqttClientContext(mqttClientContext);
            return openApi;
        }

        private void check() throws Exception {
            if (Objects.isNull(url)) {
                throw new Exception("url error");
            }
            if (Objects.isNull(productId)) {
                throw new Exception("productId error");
            }
            if (Objects.isNull(devKey)) {
                throw new Exception("devKey error");
            }
            if (Objects.isNull(accessKey)) {
                throw new Exception("accessKey error");
            }
        }
    }

    /**
     * 判断code是否成功
     *
     * @return
     */
    private boolean isSuccess(Object object) {
        JSONObject jsonObject = (JSONObject) object;
        return SUCCESS.equals(jsonObject.getInteger(KEY_CODE));
    }
}

