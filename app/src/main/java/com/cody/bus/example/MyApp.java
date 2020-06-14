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
