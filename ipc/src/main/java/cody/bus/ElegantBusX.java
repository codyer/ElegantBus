/*
 * ************************************************************
 * 文件：ElegantBusX.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月05日 20:59:58
 * 上次修改时间：2023年06月05日 20:43:19
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.Context;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;


/**
 * ElegantBus 扩展多进程支持
 */
@SuppressWarnings("unused")
public class ElegantBusX {
    private static ActivityResultLauncher<String> sLauncher;

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用 单应用多进程场景请使用{BUS_SUPPORT_MULTI_APP : false}
     * <p>
     * 多应用且多进程场景请使用{BUS_SUPPORT_MULTI_APP : true} 同时配置应用包名 {BUS_MAIN_APPLICATION_ID :共享服务且常驻的包名 } 主应用必须安装，否则不能正常运行
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
        ProcessManager.ready().support(context);
    }

    /**
     * 进程结束时调用，一般在 Application 的 onTerminate 中调用
     */
    public static void stopSupportMultiProcess() {
        ProcessManager.ready().stopSupport();
        sLauncher = null;
    }

    //

    /**
     * activity onCreate 中调用，解决某些高版本Android系统无法正常启动跨应用进程问题
     * 注意：provider 方式不需要调用此方法
     *
     * @param activity 应用启动的第一个Activity
     * @param callBack 如果用户没有授权，回调后可以提示用户授权
     */
    public static void fixHighLevelAndroid(ComponentActivity activity, ICallBack callBack) {
        if (ProcessManager.ready().isBound()) return;
        sLauncher = activity.registerForActivityResult(new HostStubActivity.RequestBusService(),
                result -> {
                    ElegantLog.d("fixHighLevelAndroid, call supportMultiProcess again. result : " + result);
                    if (result) {
                        ElegantLog.d("fixHighLevelAndroid, is bound now.");
                    } else if (callBack != null && callBack.requestPermission()) {
                        launch(activity);
                    }
                });
        launch(activity);
    }

    private static void launch(ComponentActivity activity) {
        if (sLauncher != null) {
            sLauncher.launch(ElegantUtil.getHostPackageName(activity.getApplicationContext()));
        }
    }

    public interface ICallBack {
        /**
         * 可以在回调中提示用户进行授权
         *
         * @return 是否一直请求授权，直到用户授权
         */
        boolean requestPermission();
    }
}
