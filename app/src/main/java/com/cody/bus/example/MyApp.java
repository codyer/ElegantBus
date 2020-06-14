/*
 * ************************************************************
 * 文件：MyApp.java  模块：app  项目：DoveBus
 * 当前修改时间：2020年06月14日 23:12:17
 * 上次修改时间：2020年06月14日 23:11:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：app
 * Copyright (c) 2020
 * ************************************************************
 */

package com.cody.bus.example;


import cody.bus.annotation.Event;
import cody.bus.annotation.EventGroup;

/**
 * Created by xu.yi. on 2019/4/3.
 * DoveBus
 */
@EventGroup
public class MyApp {

    @Event(value = "简单不支持多进程的事件", active = false)
    String testString;

    @Event(description = "支持多进程的事件测试对象", multiProcess = true)
    TestBean testBean;
}
