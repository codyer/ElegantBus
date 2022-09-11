/*
 * ************************************************************
 * 文件：ElegantLog.java  模块：ElegantBus.core.main  项目：ElegantBus
 * 当前修改时间：2022年09月12日 17:58:58
 * 上次修改时间：2022年09月12日 17:47:29
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.core.main
 * Copyright (c) 2022
 * ************************************************************
 */

package cody.bus;

import android.util.Log;

/**
 * 日志控制
 */
public final class ElegantLog {
    private static final String TAG = "ElegantBus";
    private static boolean isDebug = false;

    static void setDebug(final boolean debug) {
        ElegantLog.isDebug = debug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }
}
