/*
 * ************************************************************
 * 文件：EventBean.java  模块：ElegantBus.compiler.main  项目：ElegantBus
 * 当前修改时间：2022年09月12日 17:58:58
 * 上次修改时间：2022年09月12日 17:47:29
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.compiler.main
 * Copyright (c) 2022
 * ************************************************************
 */

package cody.bus.apt.bean;

/**
 * Created by xu.yi. on 2019/4/2.
 * 根据注解获取的信息
 */
public class EventBean {
    private String mEventName;
    private String mEventType;
    private String mDescription;
    private boolean mMultiProcess;
    private boolean mActive;

    public String getEventName() {
        return mEventName;
    }

    public void setEventName(String eventName) {
        this.mEventName = eventName;
    }

    public String getEventType() {
        return mEventType;
    }

    public void setEventType(String eventType) {
        this.mEventType = eventType;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public boolean isMultiProcess() {
        return mMultiProcess;
    }

    public void setMultiProcess(final boolean multiProcess) {
        this.mMultiProcess = multiProcess;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(final boolean active) {
        this.mActive = active;
    }
}
