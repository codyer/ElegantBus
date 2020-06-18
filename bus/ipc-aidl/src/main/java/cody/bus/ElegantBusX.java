/*
 * ************************************************************
 * 文件：ElegantBusX.java  模块：ipc-aidl  项目：ElegantBus
 * 当前修改时间：2020年06月18日 22:57:40
 * 上次修改时间：2020年06月18日 22:57:25
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ipc-aidl
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.content.Context;


/**
 * ElegantBus 扩展多进程支持
 */
@SuppressWarnings("unused")
public class ElegantBusX {

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用
     * 单应用多进程场景请使用
     *
     * @param context 上下文
     */
    public static void supportSingleAppMultiProcess(Context context) {
        MultiProcessImpl.support(context, context.getPackageName());
    }

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用
     * 多应用且多进程场景请使用
     *
     * @param context           上下文
     * @param mainApplicationId 共享服务且常驻的包名
     *                          如果是单应用，即为应用的包名
     *                          如果是多个应用，即为常驻的主应用的包名
     *                          主应用必须安装，否则不能正常运行
     */
    public static void supportMultiAppMultiProcess(Context context, String mainApplicationId) {
        MultiProcessImpl.support(context, mainApplicationId);
    }

    /**
     * 进程结束时调用，一般在 Application 的 onTerminate 中调用
     */
    public static void stopSupportMultiProcess() {
        MultiProcessImpl.stopSupport();
    }
}
