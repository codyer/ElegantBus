/*
 * ************************************************************
 * 文件：WrongTypeDefineException.java  模块：lib  项目：CleanFramework
 * 当前修改时间：2019年04月02日 20:52:19
 * 上次修改时间：2019年04月02日 20:52:19
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.lib.exception;

/**
 * Created by xu.yi. on 2019/3/31.
 * 在同一scope存在相同的event定义，或者这个event已经被定义
 *
 */
public class WrongTypeDefineException extends RuntimeException {
    public WrongTypeDefineException() {
        super("请使用枚举类进行事件定义");
    }
}
