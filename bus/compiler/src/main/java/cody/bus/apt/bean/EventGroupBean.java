/*
 * ************************************************************
 * 文件：EventInfoBean.java  模块：bus-compiler  项目：component
 * 当前修改时间：2019年04月23日 18:23:20
 * 上次修改时间：2019年04月13日 08:44:03
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：bus-compiler
 * Copyright (c) 2019
 * ************************************************************
 */

package cody.bus.apt.bean;

import java.util.ArrayList;

/**
 * Created by xu.yi. on 2019/4/2.
 * 解析出来的生成类信息
 */
public class EventGroupBean {
    // 注解类所在的包名
    private String mPackageName;
    // 注解类所在的类名
    private String mClassName;
    private String mGroupName;
    private boolean mActive;
    private final ArrayList<EventBean> mEventBeans = new ArrayList<>();

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

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(final String groupName) {
        this.mGroupName = groupName;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(final boolean active) {
        this.mActive = active;
    }

    public ArrayList<EventBean> getEventBeans() {
        return mEventBeans;
    }

    public void addEventBeans(EventBean eventBean) {
        mEventBeans.add(eventBean);
    }

    public String getClassString() {
        return mPackageName + "." + mClassName;
    }

    public String getEventClassPrefix() {
        return mPackageName + "." + mGroupName;
    }
}
