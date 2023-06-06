/*
 * ************************************************************
 * 文件：EventBean.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月06日 11:07:31
 * 上次修改时间：2023年06月02日 16:58:02
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "event_cache_table")
public class EventBean {
    @PrimaryKey
    @NonNull
    public String key;
    // 发送事件所在进程
    @ColumnInfo(name = "process_name")
    public String processName;
    // 发送事件到某个分组
    @ColumnInfo
    public String group;
    // 发送的事件名
    @ColumnInfo
    public String event;
    // 发送的事件类型
    @ColumnInfo
    public String type;
    // 发送的事件值的JSON串
    @ColumnInfo
    public String json;
    // 是否支持多进程
    @ColumnInfo(name = "multi_process")
    public boolean multiProcess;
    @ColumnInfo
    public boolean valid = true;
    @ColumnInfo
    public long time;

    public EventBean() {
        key = "default";
    }
}
