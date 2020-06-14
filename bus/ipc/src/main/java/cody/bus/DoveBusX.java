package cody.bus;

import android.content.Context;


/**
 * DoveBus 扩展多进程支持
 */
@SuppressWarnings("unused")
public class DoveBusX {

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
