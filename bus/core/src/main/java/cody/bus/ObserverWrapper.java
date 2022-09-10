/*
 * ************************************************************
 * 文件：ObserverWrapper.java  模块：ElegantBus.bus.core  项目：ElegantBus
 * 当前修改时间：2022年09月11日 21:47:29
 * 上次修改时间：2022年09月10日 23:04:39
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.bus.core
 * Copyright (c) 2022
 * ************************************************************
 */

package cody.bus;


import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

/**
 * Created by xu.yi. on 2019/3/31.
 * 不要主动改变属性值
 */
@SuppressWarnings("unused")
public abstract class ObserverWrapper<T> {
    LifecycleOwner owner;// 没有owner就是forever
    Observer<ValueWrapper<T>> observer;
    // 每个观察者都记录自己序号，只有在进入观察状态之后产生的数据才通知到观察者
    int sequence;
    //  优先级高的先收到事件，默认优先级为0，数字越大优先级越高
    int priority = 0;
    // 默认不是粘性事件，不会收到监听之前发送的事件
    boolean sticky = false;
    // 默认在主线程监听
    boolean uiThread = true;

    public ObserverWrapper() {
    }


    /**
     * 构造函数
     * @param priority 优先级，数字越大优先级越高
     */
    public ObserverWrapper(final int priority) {
        this(priority, false, true);
    }

    /**
     * 构造函数
     *
     * @param sticky 是否粘性事件
     */
    public ObserverWrapper(final boolean sticky) {
        this(sticky, true);
    }

    /**
     * 构造函数
     *
     * @param sticky   是否粘性事件
     * @param uiThread 是否在UI线程监听回调
     */
    public ObserverWrapper(final boolean sticky, final boolean uiThread) {
        this(0, sticky, uiThread);
    }

    /**
     * 构造函数
     * @param priority 优先级，数字越大优先级越高
     * @param sticky 是否粘性事件
     */
    public ObserverWrapper(final int priority, final boolean sticky) {
        this(priority, sticky, true);
    }

    /**
     * 构造函数
     * @param priority 优先级，数字越大优先级越高
     * @param sticky 是否粘性事件
     * @param uiThread 是否在UI线程监听回调
     */
    public ObserverWrapper(final int priority, final boolean sticky, final boolean uiThread) {
        this.priority = priority;
        this.sticky = sticky;
        this.uiThread = uiThread;
    }

    /**
     * 发生了变化
     *
     * @param value 新的值
     */
    public abstract void onChanged(@Nullable T value);
}
