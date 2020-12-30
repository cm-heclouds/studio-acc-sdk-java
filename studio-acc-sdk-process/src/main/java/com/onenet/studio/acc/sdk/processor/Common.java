package com.onenet.studio.acc.sdk.processor;

public class Common {

    /**
     * 属性id
     */
    private Integer fid;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 功能点标识
     */
    private String identifier;

    /**
     * 是否是标准功能点：自定义(u)、系统(s)或标准(sd)
     */
    private String functionType;

    /**
     * 功能点模式：tm、event或service
     */
    private String functionMode;

    /**
     * 属性描述
     */
    private String desc;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Common) {
            if (name != null && ((Common) obj).getName() != null) {
                return identifier.equals(((Common) obj).getIdentifier()) && name.equals(((Common) obj).getName());
            }
            return identifier.equals(((Common) obj).getIdentifier());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFunctionType() {
        return functionType;
    }

    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }

    public String getFunctionMode() {
        return functionMode;
    }

    public void setFunctionMode(String functionMode) {
        this.functionMode = functionMode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
