package com.onenet.studio.sdk.sample.tm;

import com.onenet.studio.acc.sdk.OpenApi;
import com.onenet.studio.acc.sdk.OpenApiExtention;
import com.onenet.studio.acc.sdk.dto.E1arraye1StructDTO;
import com.onenet.studio.acc.sdk.dto.E1infoStructDTO;
import com.onenet.studio.acc.sdk.dto.E1jsone1StructDTO;
import com.onenet.studio.acc.sdk.dto.E2warnStructDTO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
//        eventUp(extention);
    }

    /**
     * 模拟事件上报
     *
     * @author wjl
     * @date 2020/12/29
     * @param extention 物模型扩展API
     **/
    public void eventUp(OpenApiExtention extention) {
        E1infoStructDTO dto1 = new E1infoStructDTO();
        E1jsone1StructDTO dto11 = new E1jsone1StructDTO();
        dto11.setE1e2int1(2);
        dto11.setE1e2structe1(3);
        dto1.setE1jsone1(dto11);
        List<E1arraye1StructDTO> list = new ArrayList<>();
        E1arraye1StructDTO dto12 = new E1arraye1StructDTO();
        dto12.setE2testt1(true);
        list.add(dto12);
        dto1.setE1arraye1(list.toArray(new E1arraye1StructDTO[list.size()]));

        E2warnStructDTO dto2 = new E2warnStructDTO();
        dto2.setE2int1(1);
        dto2.setE2warne3(2L);

        try {
           int result = extention.eventUpload(5000, dto1, dto2);
            System.out.println("event upload result : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
