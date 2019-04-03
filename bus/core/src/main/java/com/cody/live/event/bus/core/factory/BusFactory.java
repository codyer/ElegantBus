/*
 * ************************************************************
 * 文件：BusFactory.java  模块：core  项目：CleanFramework
 * 当前修改时间：2019年04月02日 14:06:58
 * 上次修改时间：2019年04月02日 13:49:08
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus.core.factory;


import androidx.annotation.NonNull;

import com.cody.live.event.bus.core.wrapper.LiveEventWrapper;

import java.util.HashMap;

/**
 * Created by xu.yi. on 2019/3/31.
 * 和生命周期绑定的事件总线,创建基于事件的总线，对不同scope进行隔离
 */
public class BusFactory {
    private static final BusFactory sInstance = new BusFactory();
    private final HashMap<String, ScopeHolder<Object>> mScopeBus;//不同scope的bus集

    public static BusFactory ready() {
        return sInstance;
    }

    private BusFactory() {
        mScopeBus = new HashMap<>();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> LiveEventWrapper<T> create(String scope, String event) {
        ScopeHolder<Object> scopeHolder = null;
        if (mScopeBus.containsKey(scope)) {
            scopeHolder = mScopeBus.get(event);
        }
        if (scopeHolder == null) {
            scopeHolder = new ScopeHolder<>(scope, event);
            mScopeBus.put(scope, scopeHolder);
        }
        return (LiveEventWrapper<T>) scopeHolder.getBus(event);
    }

    /**
     * 每个scope一个总线集
     * 每个scope是独立的，不同scope之间事件不互通
     *
     * @param <T>
     */
    final static class ScopeHolder<T> {
        String scope;
        HashMap<String, LiveEventWrapper<T>> eventBus = new HashMap<>();

        ScopeHolder(String scopeName, String event) {
            if (!eventBus.containsKey(event)) {
                eventBus.put(event, new LiveEventWrapper<T>());
            }
            scope = scopeName;
        }

        LiveEventWrapper<T> getBus(String event) {
            LiveEventWrapper<T> bus;
            if (eventBus.containsKey(event)) {
                bus = eventBus.get(event);
            } else {
                bus = new LiveEventWrapper<>();
                eventBus.put(event, bus);
            }
            return bus;
        }
    }
}
