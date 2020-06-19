/*
 * ************************************************************
 * 文件：ProcessManagerService.java  模块：ipc-messenger  项目：ElegantBus
 * 当前修改时间：2020年06月19日 12:19:07
 * 上次修改时间：2020年06月19日 12:18:50
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ipc-messenger
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 跨进程事件总线支持服务
 * messenger 实现
 */
public class ProcessManagerService extends Service {
    public final static int MSG_REGISTER = 0x01;
    public final static int MSG_UNREGISTER = 0x02;
    public final static int MSG_ON_POST = 0x03;
    public final static int MSG_ON_POST_STICKY = 0x04;
    public final static int MSG_POST_TO_SERVICE = 0x05;
    public final static String MSG_DATA = "MSG_DATA";
    public final static String MSG_PROCESS_NAME = "MSG_PROCESS_NAME";
    public final static String CLASS_NAME = ProcessManagerService.class.getName();
    private final List<ProcessCallback> mRemoteCallbackList = new ArrayList<>();
    private final String mServiceProcessName;
    private final Map<String, EventWrapper> mEventCache = new HashMap<>();

    final class ProcessCallback {
        String processName;
        Messenger messenger;

        void onPost(EventWrapper eventWrapper) throws RemoteException {
            postValue(eventWrapper, MSG_ON_POST);
        }

        void onPostSticky(EventWrapper eventWrapper) throws RemoteException {
            postValue(eventWrapper, MSG_ON_POST_STICKY);
        }

        ProcessCallback(final String processName, final Messenger messenger) {
            this.processName = processName;
            this.messenger = messenger;
        }

        private void postValue(final EventWrapper eventWrapper, final int what) throws RemoteException {
            Message message = Message.obtain(null, what);
            message.replyTo = mServiceMessenger;
            Bundle data = new Bundle();
            data.putParcelable(ProcessManagerService.MSG_DATA, eventWrapper);
            message.setData(data);
            messenger.send(message);
        }
    }

    private Messenger mServiceMessenger = new Messenger(new ServiceHandler());

    public ProcessManagerService() {
        mServiceProcessName = Application.getProcessName();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServiceMessenger.getBinder();
    }

    @SuppressLint("HandlerLeak")
    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                // fix BadParcelableException: ClassNotFoundException when unmarshalling
                msg.getData().setClassLoader(getClass().getClassLoader());
                String processName = msg.getData().getString(ProcessManagerService.MSG_PROCESS_NAME);
                switch (msg.what) {
                    case MSG_REGISTER:
                        ProcessCallback callback = new ProcessCallback(processName, msg.replyTo);
                        mRemoteCallbackList.add(callback);
                        if (!isServiceProcess(callback)) {
                            postStickyValueToNewProcess(callback);
                        }
                        break;
                    case MSG_UNREGISTER:
                        for (ProcessCallback cb : mRemoteCallbackList) {
                            if (cb.processName.equals(processName) && cb.messenger == msg.replyTo) {
                                mRemoteCallbackList.remove(cb);
                            }
                        }
                        break;
                    case MSG_POST_TO_SERVICE:
                        EventWrapper eventWrapper = msg.getData().getParcelable(ProcessManagerService.MSG_DATA);
                        if (eventWrapper != null) {
                            putEventToCache(eventWrapper);
                            postValueToOtherProcess(eventWrapper);
                        }
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 转发事件总线
     *
     * @param eventWrapper 发送值
     */
    private void postValueToOtherProcess(EventWrapper eventWrapper) throws RemoteException {
        int count = mRemoteCallbackList.size();
        for (int i = 0; i < count; i++) {
            ProcessCallback callback = mRemoteCallbackList.get(i);
            if (callback == null) continue;
            if (isSameProcess(callback, eventWrapper.processName)) {
                ElegantLog.d("This is in same process, already posted, Event = " + eventWrapper.toString());
            } else {
                ElegantLog.d("Post new event to other process : " + callback.processName + ", Event = " + eventWrapper.toString());
                callback.onPost(eventWrapper);
            }
        }
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
    private void postStickyValueToNewProcess(final ProcessCallback callback) throws RemoteException {
        ElegantLog.d("Post all sticky event to new process : " + callback.processName);
        for (EventWrapper item : mEventCache.values()) {
            callback.onPostSticky(item);
        }
    }

    /**
     * 是否为当前管理进程
     *
     * @param callback 进程回调
     * @return 是否需要转发
     */
    private boolean isServiceProcess(ProcessCallback callback) {
        return TextUtils.equals(callback.processName, mServiceProcessName);
    }

    /**
     * 是否为发送进程不需要转发事件总线（原进程中已经处理）
     *
     * @param callback    进程回调
     * @param processName 进程名
     * @return 事件是否来自同一个进程
     */
    private boolean isSameProcess(ProcessCallback callback, String processName) {
        return TextUtils.equals(callback.processName, processName);
    }
}
