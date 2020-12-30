package com.onenet.studio.acc.sdk.processor;

import com.alibaba.fastjson.annotation.JSONField;

public class InOrOutputData {
    /**
     * struct output_data
     */
    @JSONField(name = "name")
    private String paramName;
    @JSONField(name = "identifier")
    private String paramIdentifier;
    private DataType dataType;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InOrOutputData) {
            return paramIdentifier.equals(((InOrOutputData) obj).getParamIdentifier()) && paramName.equals(((InOrOutputData) obj).getParamName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return paramIdentifier.hashCode();
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamIdentifier() {
        return paramIdentifier;
    }

    public void setParamIdentifier(String paramIdentifier) {
        this.paramIdentifier = paramIdentifier;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
}
