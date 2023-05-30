/*
 * ************************************************************
 * 文件：EventDataBase.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 09:24:55
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {EventBean.class}, version = 1, exportSchema = false)
public abstract class EventDataBase extends RoomDatabase {
    public abstract EventDao eventDao();
}
