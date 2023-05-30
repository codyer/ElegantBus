/*
 * ************************************************************
 * 文件：ElegantBus.java  模块：ElegantBus.core.main  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 17:08:38
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.core.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import androidx.annotation.NonNull;

/**
 * Created by xu.yi. on 2019/3/31. 使用 LiveData 实现类似event bus功能，支持生命周期管理
 */
@SuppressWarnings("unused")
public class ElegantBus {

    /**
     * 日志开关
     *
     * @param debug 是否打印日志
     */
    public static void setDebug(final boolean debug) {
        ElegantLog.setDebug(debug);
    }

    /**
     * 获取默认域的事件包装类
     *
     * @param event 事件名
     * @return 默认域的事件包装类
     * <p>
     * 所有事件以事件名为key进行观察 使用此方法需要自己管理事件，重名等问题，不建议使用，建议使用注解自动生成管理类
     */
    public static LiveDataWrapper<Object> getDefault(String event) {
        return getDefault(event, Object.class);
    }

    /**
     * 获取默认域的事件包装类
     *
     * @param event 事件名
     * @param type  事件类型
     * @param <T>   事件类型
     * @return 默认域的事件包装类
     * <p>
     * 使用此方法需要自己管理事件，重名等问题，不建议使用，建议使用注解自动生成管理类
     */
    public static <T> LiveDataWrapper<T> getDefault(String event, @NonNull Class<T> type) {
        return getDefault(event, type, false);
    }

    /**
     * 获取默认域的事件包装类
     *
     * @param event 事件名
     * @param type  事件类型
     * @param <T>   事件类型
     * @return 默认域的事件包装类
     * <p>
     * 使用此方法需要自己管理事件，重名等问题，不建议使用，建议使用注解自动生成管理类
     */
    public static <T> LiveDataWrapper<T> getDefault(String event, @NonNull Class<T> type, boolean multiProcess) {
        if (BusFactory.getDelegate() != null) {
            return getDefault(ElegantUtil.getHostPackageName(null), event, type, multiProcess);
        }
        return getDefault(ElegantUtil.getProcessName(), event, type, multiProcess);
    }

    /**
     * 获取默认域的事件包装类
     *
     * @param group        分组管理
     * @param event        事件名
     * @param type         事件类型
     * @param <T>          事件类型
     * @param multiProcess 是否支持跨进程
     * @return 默认域的事件包装类
     * <p>
     * 使用此方法需要自己管理事件，重名等问题，不建议使用，建议使用注解自动生成管理类
     */
    public static <T> LiveDataWrapper<T> getDefault(String group, String event, @NonNull Class<T> type,
            boolean multiProcess) {
        return BusFactory.ready()
                .create(new EventWrapper(ElegantUtil.getProcessName(), group, event, type.getName(), multiProcess));
    }

    public static <T> LiveDataWrapper<T> getStub() {
        return new StubLiveDataWrapper<>();
    }

    public static String getProcessName() {
        return ElegantUtil.getProcessName();
    }
}
