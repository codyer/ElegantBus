/*
 * ************************************************************
 * 文件：AutoGenerate.java  模块：lib  项目：CleanFramework
 * 当前修改时间：2019年04月02日 16:17:57
 * 上次修改时间：2019年04月02日 15:10:38
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
 * Created by Cody.yi on 2019/3/31.
 * 自动生成的文件注解，不要人为加上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoGenerate {
    /**
     * 范围
     */
    String value() default "DefaultScope";

    /**
     * 是否激活,可以根据需要配置是否激活事件分发，eg：debug开启，release关闭
     */
    boolean active() default true;
}
