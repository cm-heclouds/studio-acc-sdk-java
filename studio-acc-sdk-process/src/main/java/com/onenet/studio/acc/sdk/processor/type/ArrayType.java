package com.onenet.studio.acc.sdk.processor.type;

import com.onenet.studio.acc.sdk.processor.DataType;

/**
 * 数组类型
 *
 * @author wjl
 * @date 2020-09-01
 */
public class ArrayType extends DataType {

    private Integer length;

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
