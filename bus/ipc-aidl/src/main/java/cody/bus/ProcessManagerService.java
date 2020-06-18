/*
 * ************************************************************
 * 文件：ProcessManagerService.java  模块：ipc-aidl  项目：ElegantBus
 * 当前修改时间：2020年06月18日 22:57:40
 * 上次修改时间：2020年06月18日 22:57:25
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ipc-aidl
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;



/**
 * 跨进程事件总线支持服务
 * aidl 实现
 */
public class ProcessManagerService extends Service {
    public final static String CLASS_NAME = ProcessManagerService.class.getName();
    private final RemoteCallbackList<IProcessCallback> mRemoteCallbackList = new RemoteCallbackList<>();
    private final String mServiceProcessName;
    private final Map<String, EventWrapper> mEventCache = new HashMap<>();

    public ProcessManagerService() {
        mServiceProcessName = Application.getProcessName();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final Binder mBinder = new IProcessManager.Stub() {

        @Override
        public void post(final EventWrapper eventWrapper) throws RemoteException {
            putEventToCache(eventWrapper);
            postValueToOtherProcess(eventWrapper);
        }

        @Override
        public void register(IProcessCallback callback) throws RemoteException {
            mRemoteCallbackList.register(callback);
            if (!isServiceProcess(callback)) {
                postStickyValueToNewProcess(callback);
            }
        }

        @Override
        public void unregister(IProcessCallback callback) {
            mRemoteCallbackList.unregister(callback);
        }
    };

    /**
     * 转发事件总线
     *
     * @param eventWrapper 发送值
     * @throws RemoteException 异常
     */
    private void postValueToOtherProcess(EventWrapper eventWrapper) throws RemoteException {
        int count = mRemoteCallbackList.beginBroadcast();
        for (int i = 0; i < count; i++) {
            IProcessCallback callback = mRemoteCallbackList.getBroadcastItem(i);
            if (callback == null) continue;
            if (isSameProcess(callback, eventWrapper.processName)) {
                ElegantLog.d("This is in same process, already posted, Event = " + eventWrapper.toString());
            } else {
                ElegantLog.d("Post new event to other process : " + callback.processName() + ", Event = " + eventWrapper.toString());
                callback.onPost(eventWrapper);
            }
        }
        mRemoteCallbackList.finishBroadcast();
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
     * 转发 粘性事件到新的进程
     *
     * @param callback 进程回调
     * @throws RemoteException 异常
     */
    private void postStickyValueToNewProcess(final IProcessCallback callback) throws RemoteException {
        ElegantLog.d("Post all sticky event to new process : " + callback.processName());
        for (EventWrapper item : mEventCache.values()) {
            callback.onPostSticky(item);
        }
    }

    /**
     * 是否为当前管理进程
     *
     * @param callback 进程回调
     * @return 是否需要转发
     * @throws RemoteException 异常
     */
    private boolean isServiceProcess(IProcessCallback callback) throws RemoteException {
        return TextUtils.equals(callback.processName(), mServiceProcessName);
    }

    /**
     * 是否为发送进程不需要转发事件总线（原进程中已经处理）
     *
     * @param callback    进程回调
     * @param processName 进程名
     * @return 事件是否来自同一个进程
     * @throws RemoteException 异常
     */
    private boolean isSameProcess(IProcessCallback callback, String processName) throws RemoteException {
        return TextUtils.equals(callback.processName(), processName);
    }
}
