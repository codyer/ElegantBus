/*
 * ************************************************************
 * 文件：EventScopeBean.java  模块：compiler  项目：CleanFramework
 * 当前修改时间：2019年04月02日 18:29:09
 * 上次修改时间：2019年04月02日 18:29:09
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.compiler.bean;

/**
 * Created by xu.yi. on 2019/4/2.
 * CleanFramework
 */
public class EventScopeBean {
    private String name;
    private boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
