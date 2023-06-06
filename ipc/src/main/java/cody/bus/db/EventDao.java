/*
 * ************************************************************
 * 文件：EventDao.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月06日 11:07:31
 * 上次修改时间：2023年06月06日 09:46:27
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM EVENT_CACHE_TABLE where valid > 0 order by time ASC")
    Cursor getAllCursor();

    @Query("SELECT * FROM EVENT_CACHE_TABLE where valid > 0 order by time ASC")
    List<EventBean> getAllList();

    @Query("SELECT * FROM EVENT_CACHE_TABLE where `key`=:key")
    Cursor getByKeyCursor(String key);

    @Query("SELECT * FROM EVENT_CACHE_TABLE where `key`=:key")
    EventBean getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventBean event);

    @Delete
    int delete(EventBean event);

    @Update
    void update(EventBean event);
}
