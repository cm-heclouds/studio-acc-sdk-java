package com.onenet.studio.acc.sdk.processor;

import java.util.List;

/**
 * 物模型定义
 *
 * @author wjl
 * @date 2020/12/21
 *
 **/
public class Functions {

    private List<ThingsProperties> properties;
    private List<ThingsEvents> events;
    private List<ThingsServices> services;

    public List<ThingsProperties> getProperties() {
        return properties;
    }

    public void setProperties(List<ThingsProperties> properties) {
        this.properties = properties;
    }

    public List<ThingsEvents> getEvents() {
        return events;
    }

    public void setEvents(List<ThingsEvents> events) {
        this.events = events;
    }

    public List<ThingsServices> getServices() {
        return services;
    }

    public void setServices(List<ThingsServices> services) {
        this.services = services;
    }
}
