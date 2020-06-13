package com.cody.bus.example;


import com.cody.live.event.bus.lib.annotation.Event;
import com.cody.live.event.bus.lib.annotation.EventScope;

/**
 * Created by xu.yi. on 2019/4/3.
 * LiveEventBus
 */
@EventScope(name = "demo",active = true)
public enum AppDemo {
    @Event(description = "定义一个测试事件",data = String.class)testString,
    @Event(description = "定义一个测试事件测试对象",data = TestBean.class)testBean,
}
