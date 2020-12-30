package com.onenet.studio.acc.sdk.processor;

import java.util.List;

public class ThingsServices extends Common {

    private String callType;

    private List<InOrOutputData> input;

    private List<InOrOutputData> output;

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public List<InOrOutputData> getInput() {
        return input;
    }

    public void setInput(List<InOrOutputData> input) {
        this.input = input;
    }

    public List<InOrOutputData> getOutput() {
        return output;
    }

    public void setOutput(List<InOrOutputData> output) {
        this.output = output;
    }
}
