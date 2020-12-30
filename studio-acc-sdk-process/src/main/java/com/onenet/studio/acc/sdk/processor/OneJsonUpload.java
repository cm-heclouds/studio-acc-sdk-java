package com.onenet.studio.acc.sdk.processor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 设备属性上报
 *
 * @author wjl
 * @date 2020-12-23
 */
public class OneJsonUpload {

    private String id;

    private String version = "1.0";

    private Map params = new LinkedHashMap();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map getParams() {
        return params;
    }

    public void setParams(Map params) {
        this.params = params;
    }
}
