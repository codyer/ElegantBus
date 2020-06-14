/*
 * ************************************************************
 * 文件：ObserverWrapper.java  模块：bus-core  项目：component
 * 当前修改时间：2019年04月23日 18:23:19
 * 上次修改时间：2019年04月13日 08:43:55
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：bus-core
 * Copyright (c) 2019
 * ************************************************************
 */

package cody.bus;


import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

/**
 * Created by xu.yi. on 2019/3/31.
 * 不要主动改变属性值
 */
@SuppressWarnings("unused")
public abstract class ObserverWrapper<T> {
    // 每个观察者都记录自己序号，只有在进入观察状态之后产生的数据才通知到观察者
    int sequence;
    Observer<ValueWrapper<T>> observer;
    // 默认不是粘性事件，不会收到监听之前发送的事件
    boolean sticky = false;
    // 默认在主线程监听
    boolean uiThread = true;

    public ObserverWrapper() {
    }

    /**
     * 构造函数
     *
     * @param sticky 是否粘性事件
     */
    public ObserverWrapper(final boolean sticky) {
        this.sticky = sticky;
    }

    /**
     * 构造函数
     *
     * @param sticky   是否粘性事件
     * @param uiThread 是否在UI线程监听回调
     */
    public ObserverWrapper(final boolean sticky, final boolean uiThread) {
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
