/*
 * ************************************************************
 * 文件：LiveDataWrapper.java  模块：bus-core  项目：component
 * 当前修改时间：2019年04月23日 18:23:19
 * 上次修改时间：2019年04月13日 08:43:55
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：bus-core
 * Copyright (c) 2019
 * ************************************************************
 */

package cody.bus;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
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
    private String mGroupName;
    private String mEventName;
    private String mEventType;
    private boolean mProcess;
    private final MutableLiveData<ValueWrapper<T>> mMutableLiveData;

    ActiveLiveDataWrapper() {
        mMutableLiveData = new MutableLiveData<>();
    }

    ActiveLiveDataWrapper(String group, String event, String type, boolean process) {
        mGroupName = group;
        mEventName = event;
        mEventType = type;
        mProcess = process;
        mMutableLiveData = new MutableLiveData<>();
    }

    /**
     * 是否有观察者
     *
     * @return 是否有观察者
     */
    public boolean hasObservers() {
        return mMutableLiveData.hasObservers();
    }

    /**
     * 是否有激活的观察者
     *
     * @return 是否有激活的观察者
     */
    public boolean hasActiveObservers() {
        return mMutableLiveData.hasActiveObservers();
    }

    /**
     * 如果在多线程中调用，保留每一个值
     * 无需关心调用线程，只要确保在相同进程中就可以
     *
     * @param value 需要更新的值
     */
    public void post(@NonNull T value) {
        checkThread(() -> setValue(value));
        //转发到其他进程
        if (mProcess) {
            if (BusFactory.getDelegate() != null) {
                BusFactory.getDelegate().post(mGroupName, mEventName, mEventType, value);
            } else {
                Log.e(DoveBus.DOVE_TAG, "you should use DoveBusX to support multi process event bus.");
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
    public void postToCurrentProcess(@NonNull T value) {
        checkThread(() -> setValue(value));
    }

    /**
     * 只在当前进程 post 事件
     * 如果在多线程中调用，保留每一个值
     * 无需关心调用线程，只要确保在相同进程中就可以
     *
     * @param value 需要更新的值
     */
    public void postInitValue(@NonNull T value) {
        checkThread(() -> mMutableLiveData.setValue(new ValueWrapper<>(value, 0)));
    }

    /**
     * 更新事件
     * 主线程中才能使用
     *
     * @param value 更新事件值
     */
    @MainThread
    public void setValue(@NonNull T value) {
        mMutableLiveData.setValue(new ValueWrapper<>(value, mSequence));
    }

    /**
     * 主动取消观察
     *
     * @param observerWrapper 观察者包装类
     */
    public void removeObserver(@NonNull ObserverWrapper<T> observerWrapper) {
        checkThread(() -> mMutableLiveData.removeObserver(filterObserver(observerWrapper)));
    }

    /**
     * 移除某个生命周期拥有者的所有观察者
     *
     * @param owner 生命周期拥有者
     */
    public void removeObservers(@NonNull LifecycleOwner owner) {
        checkThread(() -> mMutableLiveData.removeObservers(owner));
    }

    /**
     * 和生命周期无关，全生命周期一直都监听，不用的时候需要用户自己取消监听
     *
     * @param observerWrapper 观察者包装类
     */
    public void observeForever(@NonNull final ObserverWrapper<T> observerWrapper) {
        observerWrapper.sequence = mSequence++;
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
    public void observeSticky(@NonNull LifecycleOwner owner, @NonNull ObserverWrapper<T> observerWrapper) {
        observerWrapper.sequence = -1;
        checkThread(() -> mMutableLiveData.observe(owner, filterObserver(observerWrapper)));
    }

    /**
     * 设置监听之前发送的消息不可以接收到
     * 重写 observer 的函数 isSticky ，返回true，可以实现粘性事件
     *
     * @param owner           生命周期拥有者
     * @param observerWrapper 观察者包装类
     */
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
