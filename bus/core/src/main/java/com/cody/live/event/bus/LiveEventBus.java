/*
 * ************************************************************
 * 文件：LiveEventBus.java  模块：core  项目：LiveEventBus
 * 当前修改时间：2019年04月03日 14:23:13
 * 上次修改时间：2019年04月03日 14:20:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.live.event.bus;

import com.cody.live.event.bus.core.factory.BusFactory;
import com.cody.live.event.bus.lib.IEvent;
import com.cody.live.event.bus.lib.annotation.AutoGenerate;
import com.cody.live.event.bus.lib.exception.ScopeInactiveException;
import com.cody.live.event.bus.lib.exception.WrongTypeException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by xu.yi. on 2019/3/31.
 * 使用 LiveData 实现类似event bus功能，支持生命周期管理
 * 使用方式：
 * LiveEventBus.begin()
 *                 .inScope(ToBeCompilerOut.class)
 *                 .withEvent$userDefinedEvent()
 *                 .observe(null, new ObserverWrapper&lt;String&gt;() {
 *                     Override
 *                     public void onChanged(@Nullable String s) {
 *                     }
 *                 });
 *
 *         LiveEventBus.begin()
 *                 .inScope(ToBeCompilerOut.class)
 *                 .withEvent$userDefinedEvent().postValue("");
 *         LiveEventBus.begin()
 *                 .inScope(ToBeCompilerOut.class)
 *                 .withEvent$userDefinedEvent().setValue("");
 */
public class LiveEventBus {
    private static LiveEventBus sInstance;

    public static LiveEventBus begin() {
        if (sInstance == null) {
            sInstance = new LiveEventBus();
        }
        return sInstance;
    }

    private LiveEventBus() {
    }

    /**
     * 在什么范围
     *
     * @param scopeClass 范围，通过注解自动生成的类，如果没有生成，请使用注解
     *  com.cody.live.event.bus.lib.annotation.EventScope 注解枚举类，并使用
     *  com.cody.live.event.bus.lib.annotation.Event 注解事件
     * @return 返回代理类实例
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends IEvent> T inScope(Class<T> scopeClass) {
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{scopeClass},
                new InterfaceInvokeHandler<>(scopeClass));
    }

    private class InterfaceInvokeHandler<T> implements InvocationHandler {
        private final String mScopeName;

        InterfaceInvokeHandler(Class<T> scopeClass) {
            AutoGenerate generate = scopeClass.getAnnotation(AutoGenerate.class);
            if (generate == null) {
                throw new WrongTypeException();
            }
            if (!generate.active()) {
                throw new ScopeInactiveException();
            }
            mScopeName = generate.value();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object... args) throws InvocationTargetException, IllegalAccessException {

            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            return BusFactory.ready().create(mScopeName, method.getName());
        }
    }
}
