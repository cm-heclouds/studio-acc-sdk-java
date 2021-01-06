package com.onenet.studio.acc.sdk.mqtt;

import com.alibaba.fastjson.JSONObject;
import com.onenet.studio.acc.sdk.interfaces.OpenApiCallback;
import org.eclipse.paho.client.mqttv3.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: fanhaiqiu
 * @date: 2020/12/22
 */
public class OnMessageCallback implements MqttCallback {

    private static final String KEY_ID = "id";

    private MqttClientContext context;

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
        MqttClient mqttClient = context.getMqttClient();
        while (true) {
            if (mqttClient.isConnected()) {
                break;
            }
            try {
                //每5秒式一次
                TimeUnit.SECONDS.sleep(5);
                MqttConnectOptions connOpts = context.createConnOpts();
                mqttClient.connect(connOpts);
                List<String> topic = context.getTopics();
                if (topic.size() > 0) {
                    String[] needSubTopic = new String[topic.size()];
                    mqttClient.subscribe(topic.toArray(needSubTopic));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            //下行回调类
            String reply = new String(message.getPayload());
            OpenApiCallback openApiCallback = context.getCallbackMap().get(topic);
            if (Objects.nonNull(openApiCallback)) {
                openApiCallback.callback(reply);
                return;
            }
            //上行唤醒同步返回
            JSONObject jsonObject = JSONObject.parseObject(reply);
            String id = jsonObject.getString(KEY_ID);
            Object awaitObject = context.getAwaitObject(id);
            context.putReply(id, jsonObject);
            synchronized (awaitObject) {
                awaitObject.notifyAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public void setContext(MqttClientContext context) {
        this.context = context;
    }
}
