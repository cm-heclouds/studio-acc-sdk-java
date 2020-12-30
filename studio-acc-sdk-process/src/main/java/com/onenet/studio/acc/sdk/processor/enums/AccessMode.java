package com.onenet.studio.acc.sdk.processor.enums;

/**
 * 读写类型枚举
 *
 * @author wjl
 * @date 2020-12-24
 */
public enum AccessMode {
    // 只读
    R("r"),
    // 读写
    RW("rw");

    private String mode;

    AccessMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }
}
