/*
 * ************************************************************
 * 文件：Event.java  模块：lib  项目：DoveBus
 * 当前修改时间：2020年06月14日 23:12:17
 * 上次修改时间：2020年06月14日 23:11:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：lib
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by xu.yi. on 2019/3/31.
 * 事件类型,定义在枚举值上，定义事件名称和事件携带的数据类型
 * eg: TODO
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Event {
    /**
     * @return 事件描述
     */
    String value() default "";

    /**
     * @return 同value作用一样，为了匹配实际意义添加
     */
    String description() default "";

    /**
     * @return 此事件是否支持多进程
     */
    boolean multiProcess() default false;

    /**
     * @return 是否激活, 可以根据需要配置是否激活事件分发，eg：debug开启，release关闭
     */
    boolean active() default true;
}
