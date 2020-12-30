package com.onenet.studio.acc.sdk.processor;

import java.util.List;

public class ThingsEvents extends Common{
    private String eventType;
    private List<InOrOutputData> outputData;

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<InOrOutputData> getOutputData() {
        return outputData;
    }

    public void setOutputData(List<InOrOutputData> outputData) {
        this.outputData = outputData;
    }
}
