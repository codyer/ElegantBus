/*
 * ************************************************************
 * 文件：ElegantBusService.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年06月06日 11:07:31
 * 上次修改时间：2023年06月06日 10:33:34
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import cody.bus.db.EventDataBase;


/**
 * 跨进程事件总线支持服务 aidl 实现
 */
public class ElegantBusService extends Service {

    private final Binder mBinder = new ProcessManager();

    @Override
    public void onCreate() {
        super.onCreate();
        EventDataBase.init(this, ElegantUtil.getHostPackageName(this));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
