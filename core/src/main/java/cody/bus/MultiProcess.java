/*
 * ************************************************************
 * 文件：MultiProcess.java  模块：ElegantBus.core.main  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 17:08:38
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.core.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.Context;

public interface MultiProcess {
    int MSG_ON_POST = 0x05;// service分发到当前进程的消息
    int MSG_ON_POST_STICKY = 0x06;// service分发到当前进程的Sticky消息
    int MSG_ON_RESET_STICKY = 0x07;// service分发到当前进程重置sticky
    String MSG_DATA = "MSG_DATA";

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用 多应用且多进程场景请使用
     *
     * @param context 上下文
     */
    void support(Context context);

    /**
     * 进程结束时调用，一般在 Application 的 onTerminate 中调用
     */
    void stopSupport();

    /**
     * 代理组名
     *
     * @return 主应用包名
     */
    String pkgName();

    /**
     * 发送数据到主服务
     *
     * @param eventWrapper 事件包装类
     * @param value        事件新值
     * @param <T>          值类型
     */
    <T> void postToProcessManager(EventWrapper eventWrapper, T value);

    /**
     * 重置 Sticky 序列，确保之前的值不回调
     *
     * @param eventWrapper 事件包装类
     */
    void resetSticky(EventWrapper eventWrapper);
}
