/*
 * ************************************************************
 * 文件：WrongTypeException.java  模块：core  项目：CleanFramework
 * 当前修改时间：2019年03月31日 23:54:02
 * 上次修改时间：2019年03月31日 23:54:02
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.lib.exception;

/**
 * Created by xu.yi. on 2019/3/31.
 * 类型错误异常
 */
public class WrongTypeException extends RuntimeException {
    public WrongTypeException() {
        super("请使用自动生成的接口文件");
    }
}