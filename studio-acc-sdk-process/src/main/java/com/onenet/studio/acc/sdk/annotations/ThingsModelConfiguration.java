package com.onenet.studio.acc.sdk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 物模型json文件配置
 *
 * @author wjl
 * @date 2020-12-21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface ThingsModelConfiguration {

    String value();
}
