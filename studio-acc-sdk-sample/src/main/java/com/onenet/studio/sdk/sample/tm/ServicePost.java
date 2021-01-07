package com.onenet.studio.sdk.sample.tm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onenet.studio.acc.sdk.OpenApi;
import com.onenet.studio.acc.sdk.OpenApiExtention;
import com.onenet.studio.acc.sdk.dto.RealInfoStructDTO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author wjl
 * @description 服务测试类
 * @date 2020-12-25
 */

@Component
public class ServicePost {

    @Resource
    private OpenApi openApi;

    @PostConstruct
    public void init() {
        OpenApiExtention extention = new OpenApiExtention(openApi);
        serviceInvoke(extention);
    }

    public void serviceInvoke(OpenApiExtention extention) {
        try {
            extention.realInfoServiceInvoke(oneJson -> {
                System.out.println("receive oneJson : " + oneJson);
                JSONObject jsonObject = JSON.parseObject(oneJson);

                RealInfoStructDTO dto = new RealInfoStructDTO();
                dto.setDoor(1);
                dto.setFloor(1);
                dto.setLevel(1);
                dto.setPerson(1);
                dto.setRun(1);
                dto.setSpeed(1);

                try {
                    extention.realInfoServiceInvokeReply(jsonObject.getString("id"), 200, "success", dto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
