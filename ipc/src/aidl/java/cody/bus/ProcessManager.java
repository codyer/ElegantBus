/*
 * ************************************************************
 * 文件：ProcessManager.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 10:29:02
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多进程共享一个实例
 */
public class ProcessManager extends IProcessManager.Stub {
    private final Map<String, EventWrapper> mEventCache = new ConcurrentHashMap<>();
    private final RemoteCallbackList<IProcessCallback> mRemoteCallbackList = new RemoteCallbackList<>();

    public static MultiProcess ready() {
        if (BusFactory.getDelegate() == null) {
            BusFactory.setDelegate(new MultiProcessImpl());
        }
        return BusFactory.getDelegate();
    }

    @Override
    public void register(IProcessCallback callback) throws RemoteException {
        mRemoteCallbackList.register(callback);
        if (!ElegantUtil.isServiceProcess(callback.processName())) {
            postStickyValueToNewProcess(callback);
        }
    }

    @Override
    public void unregister(IProcessCallback callback) {
        mRemoteCallbackList.unregister(callback);
    }

    @Override
    public void resetSticky(final EventWrapper eventWrapper) throws RemoteException {
        removeEventFromCache(eventWrapper);
        callbackToOtherProcess(eventWrapper, MultiProcess.MSG_ON_RESET_STICKY);
    }

    @Override
    public void postToProcessManager(final EventWrapper eventWrapper) throws RemoteException {
        putEventToCache(eventWrapper);
        callbackToOtherProcess(eventWrapper, MultiProcess.MSG_ON_POST);
    }

    /**
     * 服务进程收到事件先保留，作为其他进程的粘性事件缓存
     *
     * @param eventWrapper 消息
     */
    private void putEventToCache(final EventWrapper eventWrapper) {
        ElegantLog.d("Service receive event, add to cache, Event = " + eventWrapper.toString());
        mEventCache.put(eventWrapper.getKey(), eventWrapper);
    }

    /**
     * 服务进程收到事件先保留，作为其他进程的粘性事件缓存
     *
     * @param eventWrapper 消息
     */
    private void removeEventFromCache(final EventWrapper eventWrapper) {
        ElegantLog.d("Service receive event, remove from cache, Event = " + eventWrapper.toString());
        mEventCache.remove(eventWrapper.getKey());
    }
    
    /**
     * 转发事件总线
     *
     * @param eventWrapper 发送值
     * @throws RemoteException 异常
     */
    private void callbackToOtherProcess(EventWrapper eventWrapper, int what) throws RemoteException {
        int count = mRemoteCallbackList.beginBroadcast();
        for (int i = 0; i < count; i++) {
            IProcessCallback callback = mRemoteCallbackList.getBroadcastItem(i);
            if (callback == null) continue;
            if (ElegantUtil.isSameProcess(callback.processName(), eventWrapper.processName)) {
                ElegantLog.d("This is in same process, already posted, Event = " + eventWrapper);
            } else {
                ElegantLog.d("call back " + what + " to other process : " + callback.processName() + ", Event = " +
                        eventWrapper);
                callback.call(eventWrapper, what);
            }
        }
        mRemoteCallbackList.finishBroadcast();
    }

    /**
     * 转发 粘性事件到新的进程
     *
     * @param callback 进程回调
     * @throws RemoteException 异常
     */
    private void postStickyValueToNewProcess(final IProcessCallback callback) throws RemoteException {
        ElegantLog.d("Post all sticky event to new process : " + callback.processName());
        for (EventWrapper item : mEventCache.values()) {
            callback.call(item, MultiProcess.MSG_ON_POST_STICKY);
        }
    }
}
