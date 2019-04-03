/*
 * ************************************************************
 * 文件：EventInfoBean.java  模块：compiler  项目：CleanFramework
 * 当前修改时间：2019年04月02日 18:30:00
 * 上次修改时间：2019年04月02日 18:29:59
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.compiler.bean;

import java.util.ArrayList;

/**
 * Created by xu.yi. on 2019/4/2.
 * 解析出来的生成类信息
 */
public class EventInfoBean {
    private String mPackageName;
    private String mClassName;
    private EventScopeBean mScopeBean;
    private ArrayList<EventBean> mEventBeans = new ArrayList<>();

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public EventScopeBean getScopeBean() {
        return mScopeBean;
    }

    public void setScopeBean(EventScopeBean scopeBean) {
        mScopeBean = scopeBean;
    }

    public ArrayList<EventBean> getEventBeans() {
        return mEventBeans;
    }

    public void addEventBeans(EventBean eventBean) {
        mEventBeans.add(eventBean);
    }
}
