/*
 * ************************************************************
 * 文件：MissingEventScopeException.java  模块：core  项目：CleanFramework
 * 当前修改时间：2019年03月31日 23:56:47
 * 上次修改时间：2019年03月31日 23:56:47
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.lib.exception;

/**
 * Created by xu.yi. on 2019/3/31.
 * 事件枚举类没有使用EventScope注解
 */
public class MissingEventScopeException extends RuntimeException {
    public MissingEventScopeException() {
        super("事件枚举类没有使用EventScope注解");
    }
}
