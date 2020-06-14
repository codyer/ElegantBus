/*
 * ************************************************************
 * 文件：BusFactory.java  模块：core  项目：DoveBus
 * 当前修改时间：2020年06月14日 23:12:17
 * 上次修改时间：2020年06月14日 23:11:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：core
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;


import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
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
    private final HashMap<String, GroupHolder> mGroupBus;//不同group的bus集

    private MultiProcess mDelegate;

    interface MultiProcess {
        String hostName();

        <T> void post(String group, String event, String type, T value);
    }

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
    public <T> LiveDataWrapper<T> create(String group, String event, String type, boolean process) {
        GroupHolder groupHolder = null;
        if (mGroupBus.containsKey(group)) {
            groupHolder = mGroupBus.get(group);
        }
        if (groupHolder == null) {
            groupHolder = new GroupHolder(group, event, type, process);
            mGroupBus.put(group, groupHolder);
        }
        return groupHolder.getBus(group, event, type, process);
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
    final static class GroupHolder {
        final String group;
        final HashMap<String, LiveDataWrapper<?>> eventBus = new HashMap<>();

        GroupHolder(String groupName, String event, String type, final boolean process) {
            if (!eventBus.containsKey(event)) {
                eventBus.put(event, new ActiveLiveDataWrapper(groupName, event, type, process));
            }
            group = groupName;
        }

        @SuppressWarnings("unchecked")
        <T> LiveDataWrapper<T> getBus(String group, String event, String type, final boolean process) {
            LiveDataWrapper<T> bus;
            if (eventBus.containsKey(event)) {
                bus = (LiveDataWrapper<T>) eventBus.get(event);
            } else {
                bus = new ActiveLiveDataWrapper<>(group, event, type, process);
                eventBus.put(event, bus);
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
