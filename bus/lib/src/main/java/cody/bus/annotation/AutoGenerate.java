/*
 * ************************************************************
 * 文件：AutoGenerate.java  模块：lib  项目：DoveBus
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
 * Created by Cody.yi on 2019/3/31.
 * 自动生成的文件注解，不要人为加上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AutoGenerate {
    /**
     * @return 用在Class上表示范围
     * 用在方法上表示返回事件类型
     */
    String group() default "DefaultGroup";

    /**
     * @return 用在Class上表示范围
     * 用在方法上表示返回事件类型
     */
    String type() default "java.lang.Object";

    /**
     * 用在 Group 类上表示：是否激活,可以根据需要配置是否激活事件分发，eg：debug开启，release关闭
     * 用在 Event 方法上表示：是否激活跨进程
     *
     * @return debug开启，release关闭
     */
    boolean active() default true;

    /**
     * 用在 Group 类上表示：是否激活,可以根据需要配置是否激活事件分发，eg：debug开启，release关闭
     * 用在 Event 方法上表示：是否激活跨进程
     *
     * @return debug开启，release关闭
     */
    boolean process() default false;
}
