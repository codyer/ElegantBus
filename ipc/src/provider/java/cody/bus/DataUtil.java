/*
 * ************************************************************
 * 文件：DataUtil.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月02日 11:27:48
 * 上次修改时间：2023年06月02日 11:26:43
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.ContentValues;
import android.database.Cursor;

import cody.bus.db.BusColumnInfo;
import cody.bus.db.EventBean;

public class DataUtil {

    public static EventBean convert(EventWrapper wrapper) {
        EventBean bean = new EventBean();
        bean.key = wrapper.getKey();
        bean.processName = wrapper.processName;
        bean.group = wrapper.group;
        bean.event = wrapper.event;
        bean.type = wrapper.type;
        bean.json = wrapper.json;
        bean.multiProcess = wrapper.multiProcess;
        bean.valid = true;
        bean.time = System.currentTimeMillis();
        return bean;
    }
    /*
    public static ContentValues convert(EventWrapper wrapper) {
        ContentValues values = new ContentValues();
        values.put(BusColumnInfo.KEY, wrapper.getKey());
        values.put(BusColumnInfo.PROCESS_NAME, wrapper.processName);
        values.put(BusColumnInfo.GROUP, wrapper.group);
        values.put(BusColumnInfo.EVENT, wrapper.event);
        values.put(BusColumnInfo.TYPE, wrapper.type);
        values.put(BusColumnInfo.JSON, wrapper.json);
        values.put(BusColumnInfo.MULTI_PROCESS, wrapper.multiProcess);
        return values;
    }*/

    public static EventBean convert(ContentValues values) {
        EventBean bean = new EventBean();
        bean.key = values.getAsString(BusColumnInfo.KEY);
        bean.processName = values.getAsString(BusColumnInfo.PROCESS_NAME);
        bean.group = values.getAsString(BusColumnInfo.GROUP);
        bean.event = values.getAsString(BusColumnInfo.EVENT);
        bean.type = values.getAsString(BusColumnInfo.TYPE);
        bean.json = values.getAsString(BusColumnInfo.JSON);
        bean.multiProcess = values.getAsBoolean(BusColumnInfo.MULTI_PROCESS);
        bean.time = System.currentTimeMillis();
        return bean;
    }

    public static EventWrapper convert(Cursor cursor) {
        String key = cursor.getString(cursor.getColumnIndexOrThrow(BusColumnInfo.KEY));
        String processName = cursor.getString(cursor.getColumnIndexOrThrow(BusColumnInfo.PROCESS_NAME));
        String group = cursor.getString(cursor.getColumnIndexOrThrow(BusColumnInfo.GROUP));
        String event = cursor.getString(cursor.getColumnIndexOrThrow(BusColumnInfo.EVENT));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(BusColumnInfo.TYPE));
        String json = cursor.getString(cursor.getColumnIndexOrThrow(BusColumnInfo.JSON));
        boolean multiProcess = cursor.getInt(cursor.getColumnIndexOrThrow(BusColumnInfo.MULTI_PROCESS)) > 0;
        return new EventWrapper(processName, group, event, type, json, multiProcess);
    }
}
