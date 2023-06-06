/*
 * ************************************************************
 * 文件：EventDataBase.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月06日 11:07:31
 * 上次修改时间：2023年06月06日 10:28:01
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import cody.bus.ElegantLog;

@Database(entities = {EventBean.class}, version = 1, exportSchema = false)
public abstract class EventDataBase extends RoomDatabase {
    public abstract EventDao eventDao();

    private static volatile EventDataBase sInstance;

    public static EventDataBase getInstance() {
        if (sInstance == null) {
            ElegantLog.e("ProcessManager is not initialized.");
        }
        return sInstance;
    }

    public static void init(Context context, String pkgName) {
        if (sInstance == null) {
            synchronized (EventDataBase.class) {
                if (sInstance == null) {
                    sInstance = create(context, pkgName);
                }
            }
        }
    }

    private static EventDataBase create(final Context context, String dbName) {
        return Room.databaseBuilder(context, EventDataBase.class, dbName + ".db").build();
    }
}
