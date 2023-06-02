/*
 * ************************************************************
 * 文件：EventDao.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月02日 11:27:48
 * 上次修改时间：2023年06月02日 11:26:43
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

@Dao
public interface EventDao {
    @Query("SELECT * FROM EventBean where valid > 0 order by time ASC")
    Cursor getAllCursor();

    @Query("SELECT * FROM EventBean where `key`=:key")
    Cursor getByKey(String key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventBean event);

    @Delete
    int delete(EventBean event);

    @Update
    void update(EventBean event);
}
