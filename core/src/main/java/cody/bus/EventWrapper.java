/*
 * ************************************************************
 * 文件：EventWrapper.java  模块：ElegantBus.core.main  项目：ElegantBus
 * 当前修改时间：2022年09月12日 17:58:58
 * 上次修改时间：2022年09月12日 17:47:29
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.core.main
 * Copyright (c) 2022
 * ************************************************************
 */

package cody.bus;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 进程间缓存事件封装类，事件定义
 */
public class EventWrapper implements Parcelable {
    // 发送事件所在进程
    String processName;
    // 发送事件到某个分组
    String group;
    // 发送的事件名
    String event;
    // 发送的事件类型
    String type;
    // 发送的事件值的JSON串
    String json;
    // 是否支持多进程
    boolean multiProcess;

    public EventWrapper(final String processName, final String group, final String event, final String type, final boolean multiProcess) {
        this(processName, group, event, type, null, multiProcess);
    }

    public EventWrapper(final String processName,
                        final String group,
                        final String event,
                        final String type,
                        final String json,
                        final boolean multiProcess) {
        this.processName = processName;
        this.multiProcess = multiProcess;
        this.group = group;
        this.event = event;
        this.type = type;
        this.json = json;
    }

    protected EventWrapper(Parcel in) {
        processName = in.readString();
        group = in.readString();
        event = in.readString();
        type = in.readString();
        json = in.readString();
        multiProcess = in.readByte() != 0;
    }

    /**
     * 获取唯一值确定一个事件
     *
     * @return key
     */
    String getKey() {
        return group + event + type;
    }

    @Override
    public String toString() {
        return "{" +
                "processName='" + processName + '\'' +
                ", group='" + group + '\'' +
                ", event='" + event + '\'' +
                ", type='" + type + '\'' +
                ", json='" + json + '\'' +
                ", multiProcess=" + multiProcess +
                '}';
    }

    public static final Creator<EventWrapper> CREATOR = new Creator<EventWrapper>() {
        @Override
        public EventWrapper createFromParcel(Parcel in) {
            return new EventWrapper(in);
        }

        @Override
        public EventWrapper[] newArray(int size) {
            return new EventWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(processName);
        dest.writeString(group);
        dest.writeString(event);
        dest.writeString(type);
        dest.writeString(json);
        dest.writeByte((byte) (multiProcess ? 1 : 0));
    }
}