/*
 * ************************************************************
 * 文件：ElegantBusX.java  模块：ElegantBus.bus.ipc-binder  项目：ElegantBus
 * 当前修改时间：2021年08月15日 17:27:55
 * 上次修改时间：2021年08月15日 17:26:11
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.bus.ipc-binder
 * Copyright (c) 2021
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
     * 单应用多进程场景请使用{BUS_SUPPORT_MULTI_APP : false}
     * <p>
     * 多应用且多进程场景请使用{BUS_SUPPORT_MULTI_APP : true}
     * 同时配置应用包名 {BUS_MAIN_APPLICATION_ID :共享服务且常驻的包名 }
     * 主应用必须安装，否则不能正常运行
     * <p>
     * eg:<pre><code>{
     *      manifestPlaceholders = [
     *         BUS_SUPPORT_MULTI_APP  : true,
     *         BUS_MAIN_APPLICATION_ID: "com.example.bus"
     *     ]
     * }</code></pre>
     *
     * @param context 上下文
     */
    public static void supportMultiProcess(Context context) {
        MultiProcessImpl.ready().support(context);
    }

    /**
     * 进程结束时调用，一般在 Application 的 onTerminate 中调用
     */
    public static void stopSupportMultiProcess() {
        MultiProcessImpl.ready().stopSupport();
    }
}
