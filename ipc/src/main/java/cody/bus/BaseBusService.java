/*
 * ************************************************************
 * 文件：BaseBusService.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年02月24日 18:35:16
 * 上次修改时间：2023年02月24日 18:35:12
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


/**
 * 跨进程事件总线支持服务
 */
public abstract class BaseBusService extends Service {
    private String getNotificationChannelId() {
        return new ComponentName(this, BaseBusService.class).toShortString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(
                    getNotificationChannelId(), getNotificationChannelId(),
                    NotificationManager.IMPORTANCE_DEFAULT));
            Notification notification = new Notification
                    .Builder(getApplicationContext(), getNotificationChannelId())
                    .setContentTitle("BusService")
                    .setSmallIcon(android.R.drawable.ic_popup_sync)
                    .build();
            startForeground(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void startUserService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
