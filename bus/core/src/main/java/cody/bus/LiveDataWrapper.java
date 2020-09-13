/*
 * ************************************************************
 * 文件：LiveDataWrapper.java  模块：core  项目：ElegantBus
 * 当前修改时间：2020年09月13日 09:43:44
 * 上次修改时间：2020年09月13日 09:39:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：core
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;


/**
 * Created by xu.yi. on 2019/3/31.
 * 和lifecycle绑定的事件总线
 * 每添加一个observer，LiveDataWrapper 的序列号增加1，并赋值给新加的observer，
 * 每次消息更新使用目前的序列号进行请求，持有更小的序列号才需要获取变更通知。
 * <p>
 * 解决会收到注册前发送的消息更新问题
 */
public interface LiveDataWrapper<T> {

    /**
     * 是否有观察者
     *
     * @return 是否有观察者
     */
    boolean hasObservers();

    /**
     * 是否有激活的观察者
     *
     * @return 是否有激活的观察者
     */
    boolean hasActiveObservers();

    /**
     * 获取最后保留的值，比如登录状态 可能会没有初始化就会没有值
     *
     * @return 获取最后保留的值
     */
    @Nullable
    T getValue();

    /**
     * 如果在多线程中调用，保留每一个值
     * 无需关心调用线程，只要确保在相同进程中就可以
     *
     * @param value 需要更新的值
     */
    void post(@NonNull T value);

    /**
     * 只在当前进程 post 事件
     * 如果在多线程中调用，保留每一个值
     * 无需关心调用线程，只要确保在相同进程中就可以
     *
     * @param value 需要更新的值
     */
    void postToCurrentProcess(@NonNull T value);

    /**
     * 跨进程的粘性事件支持，新建进程时，需要初始值时调用，其他情况不要使用
     *
     * @param value 需要更新的值
     */
    void postStickyToCurrentProcess(@NonNull T value);

    /**
     * 更新事件
     * 主线程中才能使用
     *
     * @param value 更新事件值
     */
    @MainThread
    void setValue(@NonNull T value);

    /**
     * 主动取消观察
     *
     * @param observerWrapper 观察者包装类
     */
    void removeObserver(@NonNull ObserverWrapper<T> observerWrapper);

    /**
     * 移除某个生命周期拥有者的所有观察者
     *
     * @param owner 生命周期拥有者
     */
    void removeObservers(@NonNull LifecycleOwner owner);

    /**
     * 和生命周期无关，全生命周期一直都监听，不用的时候需要用户自己取消监听
     *
     * @param observerWrapper 观察者包装类
     */
    void observeForever(@NonNull final ObserverWrapper<T> observerWrapper);

    /**
     * 粘性事件，设置监听之前发送的消息也可以接收到
     * 重写 observer 的函数 isSticky ，返回true，可以实现粘性事件
     *
     * @param owner           生命周期拥有者
     * @param observerWrapper 观察者包装类
     * @see #observe(LifecycleOwner, ObserverWrapper)
     */
    void observeSticky(@NonNull LifecycleOwner owner, @NonNull ObserverWrapper<T> observerWrapper);

    /**
     * 设置监听之前发送的消息不可以接收到
     * 重写 observer 的函数 isSticky ，返回true，可以实现粘性事件
     *
     * @param owner           生命周期拥有者
     * @param observerWrapper 观察者包装类
     */
    void observe(@NonNull LifecycleOwner owner, @NonNull ObserverWrapper<T> observerWrapper);
}
