/*
 * ************************************************************
 * 文件：DoveBus.java  模块：core  项目：DoveBus
 * 当前修改时间：2020年06月14日 23:12:17
 * 上次修改时间：2020年06月14日 23:11:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：core
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.app.Application;

import androidx.annotation.NonNull;

/**
 * Created by xu.yi. on 2019/3/31.
 * 使用 LiveData 实现类似event bus功能，支持生命周期管理
 */
@SuppressWarnings("unused")
public class DoveBus {
    public final static String DOVE_TAG = "DoveBus";

    /**
     * 获取默认域的事件包装类
     *
     * @param event 事件名
     * @return 默认域的事件包装类
     * <p>
     * 所有事件以事件名为key进行观察
     * 使用此方法需要自己管理事件，重名等问题，不建议使用，建议使用注解自动生成管理类
     */
    public static LiveDataWrapper getDefault(String event) {
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
    public static <T> LiveDataWrapper<T> getDefault(String event, @NonNull Class<T> type, boolean process) {
        if (BusFactory.getDelegate() != null) {
            return getDefault(BusFactory.getDelegate().hostName(), event, type, process);
        }
        return getDefault(Application.getProcessName(), event, type, process);
    }

    /**
     * 获取默认域的事件包装类
     *
     * @param group   分组管理
     * @param event   事件名
     * @param type    事件类型
     * @param <T>     事件类型
     * @param process 是否支持跨进程
     * @return 默认域的事件包装类
     * <p>
     * 使用此方法需要自己管理事件，重名等问题，不建议使用，建议使用注解自动生成管理类
     */
    public static <T> LiveDataWrapper<T> getDefault(String group, String event, @NonNull Class<T> type, boolean process) {
        return BusFactory.ready().create(group, event, type.getName(), process);
    }

    public static <T> LiveDataWrapper<T> getStub() {
        return new StubLiveDataWrapper<>();
    }
}
