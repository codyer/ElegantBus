/*
 * ************************************************************
 * 文件：BusFactory.java  模块：ElegantBus.bus.core  项目：ElegantBus
 * 当前修改时间：2021年08月15日 01:18:42
 * 上次修改时间：2021年08月15日 01:18:38
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.bus.core
 * Copyright (c) 2021
 * ************************************************************
 */

package cody.bus;


import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

/**
 * Created by xu.yi. on 2019/3/31.
 * 和生命周期绑定的事件总线,创建基于事件的总线，对不同group进行隔离
 */
class BusFactory {
    private final Object mLock = new Object();
    private volatile Handler mMainHandler;
    private final ExecutorService mExecutorService;
    //不同group的bus集
    private final Map<String, EventGroupHolder> mGroupBus;
    private MultiProcess mDelegate;

    static void setDelegate(final MultiProcess MultiProcess) {
        ready().mDelegate = MultiProcess;
    }

    static MultiProcess getDelegate() {
        return ready().mDelegate;
    }

    private static class InstanceHolder {
        private static final BusFactory INSTANCE = new BusFactory();
    }

    public static BusFactory ready() {
        return InstanceHolder.INSTANCE;
    }

    private BusFactory() {
        mGroupBus = new HashMap<>();
        mExecutorService = Executors.newCachedThreadPool();
    }

    @NonNull
    public <T> LiveDataWrapper<T> create(EventWrapper eventWrapper) {
        EventGroupHolder eventGroupHolder = null;
        if (mGroupBus.containsKey(eventWrapper.group)) {
            eventGroupHolder = mGroupBus.get(eventWrapper.group);
        }
        if (eventGroupHolder == null) {
            synchronized (mGroupBus) {
                if (mGroupBus.containsKey(eventWrapper.group)) {
                    eventGroupHolder = mGroupBus.get(eventWrapper.group);
                } else {
                    eventGroupHolder = new EventGroupHolder(eventWrapper);
                    mGroupBus.put(eventWrapper.group, eventGroupHolder);
                }
            }
        }
        assert eventGroupHolder != null;
        return eventGroupHolder.getBus(eventWrapper);
    }

    ExecutorService getExecutorService() {
        return mExecutorService;
    }

    Handler getMainHandler() {
        if (mMainHandler == null) {
            synchronized (mLock) {
                if (mMainHandler == null) {
                    mMainHandler = createAsync(Looper.getMainLooper());
                }
            }
        }
        return mMainHandler;
    }

    /**
     * 每个group一个总线集
     * 每个group是独立的，不同group之间事件不互通
     */
    final static class EventGroupHolder {
        final Map<String, LiveDataWrapper<?>> eventBus = new HashMap<>();

        EventGroupHolder(EventWrapper eventWrapper) {
            if (!eventBus.containsKey(eventWrapper.event)) {
                eventBus.put(eventWrapper.event, new ActiveLiveDataWrapper(eventWrapper));
            }
        }

        @SuppressWarnings("unchecked")
        <T> LiveDataWrapper<T> getBus(EventWrapper eventWrapper) {
            LiveDataWrapper<T> bus;
            //  一个分组不会有相同的事件名，即使类型不一样也不行，定义事件时就会报错
            // 如果用户不使用事件定义方式，很难保证事件一致
            if (eventBus.containsKey(eventWrapper.event)) {
                bus = (LiveDataWrapper<T>) eventBus.get(eventWrapper.event);
            } else {
                synchronized (eventBus) {
                    if (eventBus.containsKey(eventWrapper.event)) {
                        bus = (LiveDataWrapper<T>) eventBus.get(eventWrapper.event);
                    } else {
                        bus = new ActiveLiveDataWrapper<>(eventWrapper);
                        eventBus.put(eventWrapper.event, bus);
                    }
                }
            }
            return bus;
        }
    }

    private static Handler createAsync(@NonNull Looper looper) {
        if (Build.VERSION.SDK_INT >= 28) {
            return Handler.createAsync(looper);
        }
        return new Handler(looper);
    }
}
