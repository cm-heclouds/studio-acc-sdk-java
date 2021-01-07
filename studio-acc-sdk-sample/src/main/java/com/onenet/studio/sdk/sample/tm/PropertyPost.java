package com.onenet.studio.sdk.sample.tm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.onenet.studio.acc.sdk.OpenApi;
import com.onenet.studio.acc.sdk.OpenApiExtention;
import com.onenet.studio.acc.sdk.dto.LiftStructDTO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: fanhaiqiu
 * @date: 2020/12/24
 */
@Component
public class PropertyPost {

    @Resource
    private OpenApi openApi;

    @PostConstruct
    public void init() {
        OpenApiExtention extention = new OpenApiExtention(openApi);
        propertyUp(extention);
        propertySet(extention);
        peropertyGet(extention);
        desiredGet(extention);
//        desiredDel(extention);
    }

    /**
     * 属性上报
     *
     * @author wjl
     * @date 2020/12/29
     * @param extention 物模型扩展API
     **/
    public void propertyUp(OpenApiExtention extention) {
        LiftStructDTO dto = new LiftStructDTO();
        dto.setDoor(1);
        dto.setFloor(1);
        dto.setLevel(1);
        dto.setPerson(1);
        dto.setRun(1);
        dto.setSpeed(1);

        try {
            int result = extention.propertyUpload(5000, 1, "1.0", dto);
            System.out.println("up result : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 属性设置
     *
     * @author wjl
     * @date 2020/12/29
     * @param extention 物模型扩展API
     **/
    public void propertySet(OpenApiExtention extention) {
        try {
            extention.propertySet(oneJson -> {
                System.out.println("receive oneJson : " + oneJson);
                JSONObject json = JSON.parseObject(oneJson);
                try {
                    extention.propertySetReply(json.getString("id"), 200, "success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 属性获取
     *
     * @author wjl
     * @date 2020/12/29
     * @param extention 物模型扩展API
     **/
    public void peropertyGet(OpenApiExtention extention) {
        try {
            extention.propertyGet(oneJson -> {
                System.out.println("receive oneJson : " + oneJson);
                JSONObject json = JSON.parseObject(oneJson);
                try {
                    // 返回的属性与收到的属性要对应上，否则不成功
                    Map<String, Object> map = new HashMap<>();
                    map.put("DeviceStatus", 1);
                    extention.propertyGetReply(json.getString("id"), 200, "success", map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 期望值获取
     *
     * @author wjl
     * @date 2020/12/29
     * @param extention 物模型扩展API
     **/
    public void desiredGet(OpenApiExtention extention) {
        List<String> list = new ArrayList<>();
        list.add("DeviceStatus");

        try {
            JSONObject get = extention.propertyDesiredGet(list.toArray(new String[list.size()]), 5000);
            System.out.println("desired get oneJson : " + get.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 期望值删除
     *
     * @author wjl
     * @date 2020/12/29
     * @param extention 物模型扩展API
     * @return void
     **/
    public void desiredDel(OpenApiExtention extention) {
        Map<String, Integer> map = new HashMap<>();
        map.put("DeviceStatus", 1);
        try {
            int result = extention.propertyDesiredDel(5000, map);
            System.out.println("desired del result : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
