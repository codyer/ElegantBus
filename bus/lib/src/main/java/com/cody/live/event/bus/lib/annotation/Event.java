/*
 * ************************************************************
 * 文件：Event.java  模块：lib  项目：CleanFramework
 * 当前修改时间：2019年04月02日 15:07:43
 * 上次修改时间：2019年04月02日 15:07:42
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.lib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by xu.yi. on 2019/3/31.
 * 事件类型,定义在枚举值上，定义事件名称和事件携带的数据类型
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Event {
    /**
     * 事件描述
     */
    String value() default "";

    /**
     * 同value作用一样，为了匹配实际意义添加
     */
    String description() default "";

    /**
     * 事件携带数据类型
     */
    Class data() default Object.class;
}
