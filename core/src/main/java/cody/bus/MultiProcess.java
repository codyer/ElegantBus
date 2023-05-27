/*
 * ************************************************************
 * 文件：MultiProcess.java  模块：ElegantBus.core.main  项目：ElegantBus
 * 当前修改时间：2023年05月27日 12:23:16
 * 上次修改时间：2023年05月27日 12:20:43
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.core.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.Context;

import com.alibaba.fastjson.JSON;

public interface MultiProcess {
    int MSG_ON_POST = 0x05;// service分发到当前进程的消息
    int MSG_ON_POST_STICKY = 0x06;// service分发到当前进程的Sticky消息
    int MSG_ON_RESET_STICKY = 0x07;// service分发到当前进程重置sticky

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用
     * 多应用且多进程场景请使用
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
    <T> void postToService(EventWrapper eventWrapper, T value);

    /**
     * 重置 Sticky 序列，确保之前的值不回调
     *
     * @param eventWrapper 事件包装类
     */
    void resetSticky(EventWrapper eventWrapper);

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
