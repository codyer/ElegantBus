/*
 * ************************************************************
 * 文件：ElegantUtil.java  模块：ElegantBus.core.main  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 11:56:31
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.core.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class ElegantUtil {

    private static String mCurrentProcessName;
    private static String mHostPackageName;
    private final static String mProcessManagerSubName = ".ElegantBusProcessManager";

    /**
     * 获取当前进程名
     *
     * @return 进程名
     */
    static String getProcessName() {
        if (!TextUtils.isEmpty(mCurrentProcessName)) {
            return mCurrentProcessName;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + android.os.Process.myPid() + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                mCurrentProcessName = processName.trim();
            }
            return mCurrentProcessName;
        } catch (Throwable throwable) {
            ElegantLog.e(Log.getStackTraceString(throwable));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                ElegantLog.e(Log.getStackTraceString(exception));
            }
        }
        return "default";
    }

    /**
     * 是否为当前管理进程
     *
     * @param processName 进程名
     * @return 是否需要转发
     */
    static boolean isServiceProcess(String processName) {
        return TextUtils.equals(processName, mHostPackageName + mProcessManagerSubName);
    }

    /**
     * 是否为发送进程不需要转发事件总线（原进程中已经处理）
     *
     * @param currentProcessName 进程回调进程名
     * @param processName        进程名
     * @return 事件是否来自同一个进程
     */
    static boolean isSameProcess(String currentProcessName, String processName) {
        return TextUtils.equals(currentProcessName, processName);
    }

    static String getHostPackageName(Context context) {
        if (!TextUtils.isEmpty(mHostPackageName)) {
            return mHostPackageName;
        }
        if (context != null) {
            try {
                ApplicationInfo
                        info = context.getPackageManager()
                        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                boolean supportMultiApp = info.metaData.getBoolean("BUS_SUPPORT_MULTI_APP", false);
                if (!supportMultiApp) {
                    mHostPackageName = context.getPackageName();
                } else {
                    String mainApplicationId = info.metaData.getString("BUS_MAIN_APPLICATION_ID");
                    if (TextUtils.isEmpty(mainApplicationId)) {
                        ElegantLog.e("Must config {BUS_MAIN_APPLICATION_ID} in manifestPlaceholders .");
                        if (ElegantLog.isDebug()) {
                            throw new RuntimeException(
                                    "Must config {BUS_MAIN_APPLICATION_ID} in manifestPlaceholders .");
                        }
                        return null;
                    } else {
                        mHostPackageName = mainApplicationId;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return mHostPackageName;
    }

    /**
     * 发送数据前进行编码
     *
     * @param eventWrapper 事件包装类
     * @param value        值
     * @param <T>          类型
     * @return 事件包装类
     */
    static <T> EventWrapper encode(EventWrapper eventWrapper, T value) {
        eventWrapper.json = JSON.toJSONString(value);
        return eventWrapper;
    }

    /**
     * 接收数据后进行解码
     *
     * @param eventWrapper 事件包装类
     * @param what         消息
     */
    static void decode(final EventWrapper eventWrapper, final int what) {
        BusFactory.ready().getSingleExecutorService().execute(() -> {
            Object value = null;
            switch (what) {
                case MultiProcess.MSG_ON_POST:
                    try {
                        value = JSON.parseObject(eventWrapper.json, Class.forName(eventWrapper.type));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (value == null) {
                        ElegantLog.e(" MSG_ON_POST value is null" + eventWrapper);
                        return;
                    }
                    BusFactory.ready().create(eventWrapper).postToCurrentProcess(value);
                    break;
                case MultiProcess.MSG_ON_POST_STICKY:
                    try {
                        value = JSON.parseObject(eventWrapper.json, Class.forName(eventWrapper.type));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (value == null) {
                        ElegantLog.e(" MSG_ON_POST value is null" + eventWrapper);
                        return;
                    }
                    BusFactory.ready().create(eventWrapper).postStickyToCurrentProcess(value);
                    break;
                case MultiProcess.MSG_ON_RESET_STICKY:
                    BusFactory.ready().create(eventWrapper).resetStickyToCurrentProcess();
                    break;
            }
        });
    }
}
