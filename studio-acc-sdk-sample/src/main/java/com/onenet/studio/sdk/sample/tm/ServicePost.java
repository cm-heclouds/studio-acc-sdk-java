package com.onenet.studio.sdk.sample.tm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onenet.studio.acc.sdk.OpenApi;
import com.onenet.studio.acc.sdk.OpenApiExtention;
import com.onenet.studio.acc.sdk.dto.S1s2tStructDTO;
import com.onenet.studio.acc.sdk.dto.S1testStructDTO;
import com.onenet.studio.acc.sdk.dto.S2s1pStructDTO;
import com.onenet.studio.acc.sdk.dto.S2testStructDTO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
        //serviceInvoke(extention);
    }

    public void serviceInvoke(OpenApiExtention extention) {
        try {
            extention.s1testServiceInvoke(oneJson -> {
                System.out.println("s1test receive oneJson : " + oneJson);
                JSONObject jsonObject = JSON.parseObject(oneJson);
                S1testStructDTO dto = new S1testStructDTO();
                S1s2tStructDTO dto1 = new S1s2tStructDTO();
                dto1.setS1s2t2(3.2);
                dto1.setS1s2t4(3L);
                dto.setS1s2t(dto1);
                dto.setServicetest1(5);
                try {
                    extention.s1testServiceInvokeReply(jsonObject.getString("id"), 200, "success", dto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            extention.s2testServiceInvoke(oneJson -> {
                System.out.println("s2test receive oneJson : " + oneJson);
                JSONObject jsonObject = JSON.parseObject(oneJson);
                S2testStructDTO dto = new S2testStructDTO();
                List<S2s1pStructDTO> list = new ArrayList<>();
                S2s1pStructDTO dto1 = new S2s1pStructDTO();
                dto1.setS2s1p8(01L);
                dto1.setS2s1t8(50);
                list. add(dto1);
                dto.setS2s1p(list.toArray(new S2s1pStructDTO[list.size()]));

                try {
                    extention.s2testServiceInvokeReply(jsonObject.getString("id"), 200, "success", dto);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
