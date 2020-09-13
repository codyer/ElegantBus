/*
 * ************************************************************
 * 文件：ActiveLiveDataWrapper.java  模块：core  项目：ElegantBus
 * 当前修改时间：2020年09月13日 09:43:44
 * 上次修改时间：2020年09月13日 09:39:57
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：core
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


/**
 * Created by xu.yi. on 2019/3/31.
 * 和lifecycle绑定的事件总线
 * 每添加一个observer，LiveDataWrapper 的序列号增加1，并赋值给新加的observer，
 * 每次消息更新使用目前的序列号进行请求，持有更小的序列号才需要获取变更通知。
 * <p>
 * 解决会收到注册前发送的消息更新问题
 */
@SuppressWarnings("unused")
public class ActiveLiveDataWrapper<T> implements LiveDataWrapper<T> {
    private int mSequence = 0;
    private EventWrapper mEventWrapper;
    private final MutableLiveData<ValueWrapper<T>> mMutableLiveData;

    ActiveLiveDataWrapper() {
        mMutableLiveData = new MutableLiveData<>();
    }

    ActiveLiveDataWrapper(final EventWrapper eventWrapper) {
        mEventWrapper = eventWrapper;
        mMutableLiveData = new MutableLiveData<>();
    }

    /**
     * 是否有观察者
     *
     * @return 是否有观察者
     */
    @Override
    public boolean hasObservers() {
        return mMutableLiveData.hasObservers();
    }

    /**
     * 是否有激活的观察者
     *
     * @return 是否有激活的观察者
     */
    @Override
    public boolean hasActiveObservers() {
        return mMutableLiveData.hasActiveObservers();
    }

    /**
     * 获取最后保留的值，比如登录状态 可能会没有初始化就会没有值
     *
     * @return 获取最后保留的值
     */
    @Nullable
    @Override
    public T getValue() {
        if (mMutableLiveData.getValue() == null) {
            return null;
        }
        return mMutableLiveData.getValue().value;
    }

    /**
     * 如果在多线程中调用，保留每一个值
     * 无需关心调用线程，只要确保在相同进程中就可以
     *
     * @param value 需要更新的值
     */
    @Override
    public void post(@NonNull T value) {
        checkThread(() -> setValue(value));
        //转发到其他进程
        if (mEventWrapper.multiProcess) {
            if (BusFactory.getDelegate() != null) {
                BusFactory.getDelegate().postToService(mEventWrapper, value);
            } else {
                ElegantLog.w("you should use ElegantBusX to support multi process event bus.");
            }
        }
    }

    /**
     * 只在当前进程 post 事件
     * 如果在多线程中调用，保留每一个值
     * 无需关心调用线程，只要确保在相同进程中就可以
     *
     * @param value 需要更新的值
     */
    @Override
    public void postToCurrentProcess(@NonNull T value) {
        checkThread(() -> setValue(value));
    }

    /**
     * 跨进程的粘性事件支持，新建进程时，需要初始值时调用，其他情况不要使用
     *
     * @param value 需要更新的值
     */
    @Override
    public void postStickyToCurrentProcess(@NonNull T value) {
        checkThread(() -> mMutableLiveData.setValue(new ValueWrapper<>(value, 0)));
    }

    /**
     * 更新事件
     * 主线程中才能使用
     *
     * @param value 更新事件值
     */
    @MainThread
    @Override
    public void setValue(@NonNull T value) {
        mMutableLiveData.setValue(new ValueWrapper<>(value, mSequence));
    }

    /**
     * 主动取消观察
     *
     * @param observerWrapper 观察者包装类
     */
    @Override
    public void removeObserver(@NonNull ObserverWrapper<T> observerWrapper) {
        checkThread(() -> mMutableLiveData.removeObserver(filterObserver(observerWrapper)));
    }

    /**
     * 移除某个生命周期拥有者的所有观察者
     *
     * @param owner 生命周期拥有者
     */
    @Override
    public void removeObservers(@NonNull LifecycleOwner owner) {
        checkThread(() -> mMutableLiveData.removeObservers(owner));
    }

    /**
     * 和生命周期无关，全生命周期一直都监听，不用的时候需要用户自己取消监听
     *
     * @param observerWrapper 观察者包装类
     */
    @Override
    public void observeForever(@NonNull final ObserverWrapper<T> observerWrapper) {
        observerWrapper.sequence = observerWrapper.sticky ? -1 : mSequence++;
        checkThread(() -> mMutableLiveData.observeForever(filterObserver(observerWrapper)));
    }

    /**
     * 粘性事件，设置监听之前发送的消息也可以接收到
     * 重写 observer 的函数 isSticky ，返回true，可以实现粘性事件
     *
     * @param owner           生命周期拥有者
     * @param observerWrapper 观察者包装类
     * @see #observe(LifecycleOwner, ObserverWrapper)
     */
    @Override
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull ObserverWrapper<T> observerWrapper) {
        observerWrapper.sticky = true;
        observe(owner, observerWrapper);
    }

    /**
     * 设置监听之前发送的消息不可以接收到
     * 重写 observer 的函数 isSticky ，返回true，可以实现粘性事件
     *
     * @param owner           生命周期拥有者
     * @param observerWrapper 观察者包装类
     */
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull ObserverWrapper<T> observerWrapper) {
        observerWrapper.sequence = observerWrapper.sticky ? -1 : mSequence++;
        checkThread(() -> mMutableLiveData.observe(owner, filterObserver(observerWrapper)));
    }

    /**
     * 从包装类中过滤出原始观察者
     *
     * @param observerWrapper 包装类
     * @return 原始观察者
     */
    @NonNull
    private Observer<ValueWrapper<T>> filterObserver(@NonNull final ObserverWrapper<T> observerWrapper) {
        if (observerWrapper.observer != null) {
            return observerWrapper.observer;
        }
        return observerWrapper.observer = valueWrapper -> {
            // 产生的事件序号要大于观察者序号才被通知事件变化
            if (valueWrapper != null && valueWrapper.sequence > observerWrapper.sequence) {
                if (observerWrapper.uiThread) {
                    observerWrapper.onChanged(valueWrapper.value);
                } else {
                    BusFactory
                            .ready()
                            .getExecutorService()
                            .execute(() -> observerWrapper.onChanged(valueWrapper.value));
                }
            }
        };
    }

    /**
     * 是否是在主线程
     *
     * @return 是主线程
     */
    private boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * 检查线程并执行不同的操作
     *
     * @param runnable 可运行的一段代码
     */
    private void checkThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            // 主线程中观察
            BusFactory
                    .ready()
                    .getMainHandler()
                    .post(runnable);
        }
    }
}
