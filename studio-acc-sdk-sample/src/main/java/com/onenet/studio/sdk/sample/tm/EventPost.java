package com.onenet.studio.sdk.sample.tm;

import com.onenet.studio.acc.sdk.OpenApi;
import com.onenet.studio.acc.sdk.OpenApiExtention;
import com.onenet.studio.acc.sdk.dto.FaultInfoStructDTO;
import com.onenet.studio.acc.sdk.dto.FaultStructDTO;
import com.onenet.studio.acc.sdk.dto.WorkInfoStructDTO;
import com.onenet.studio.acc.sdk.dto.WorkStructDTO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 物模型事件测试类
 *
 * @author wjl
 * @date 2020-12-25
 */
@Component
public class EventPost {

    @Resource
    private OpenApi openApi;

    @PostConstruct
    public void init() {
        OpenApiExtention extention = new OpenApiExtention(openApi);
        eventUp(extention);
    }

    /**
     * 模拟事件上报
     *
     * @author wjl
     * @date 2020/12/29
     * @param extention 物模型扩展API
     **/
    public void eventUp(OpenApiExtention extention) {
        FaultStructDTO dto1 = new FaultStructDTO();
        dto1.setCode("200");
        dto1.setState(1);
        FaultInfoStructDTO dto11 = new FaultInfoStructDTO();
        dto11.setDoor(1);
        dto11.setFloor(1);
        dto11.setLevel(1);
        dto11.setPerson(1);
        dto11.setRun(1);
        dto11.setSpeed(1);
        dto1.setInfo(dto11);

        WorkStructDTO dto2 = new WorkStructDTO();
        dto2.setPoint(1L);
        dto2.setSec(1);
        WorkInfoStructDTO dto21 = new WorkInfoStructDTO();
        dto21.setDoor(1);
        dto21.setFloor(1);
        dto21.setLevel(1);
        dto21.setPerson(1);
        dto21.setRun(1);
        dto21.setSpeed(1f);
        dto2.setInfo(dto21);

        try {
           int result = extention.eventUpload(5000, dto1, null);
            System.out.println("event upload result : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
