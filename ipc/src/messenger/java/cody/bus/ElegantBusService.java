/*
 * ************************************************************
 * 文件：ElegantBusService.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月06日 11:07:31
 * 上次修改时间：2023年06月06日 10:40:09
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cody.bus.db.EventBean;
import cody.bus.db.EventDataBase;


/**
 * 跨进程事件总线支持服务 messenger 实现
 */
public class ElegantBusService extends Service {
    public final static int MSG_REGISTER = 0x01;
    public final static int MSG_UNREGISTER = 0x02;
    public final static int MSG_RESET_STICKY = 0x03;//进程重置sticky
    public final static int MSG_POST_TO_SERVICE = 0x04;//进程分发到service
    public final static String MSG_PROCESS_NAME = "MSG_PROCESS_NAME";
    private final List<ProcessCallback> mRemoteCallbackList = new CopyOnWriteArrayList<>();
    private final Messenger mServiceMessenger = new Messenger(new ServiceHandler(Looper.getMainLooper()));

    @Override
    public void onCreate() {
        super.onCreate();
        EventDataBase.init(this, ElegantUtil.getHostPackageName(this));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServiceMessenger.getBinder();
    }

    @SuppressLint("HandlerLeak")
    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                // fix BadParcelableException: ClassNotFoundException when unmarshalling
                msg.getData().setClassLoader(getClass().getClassLoader());
                String processName = msg.getData().getString(ElegantBusService.MSG_PROCESS_NAME);
                EventWrapper eventWrapper;
                switch (msg.what) {
                    case MSG_REGISTER:
                        ProcessCallback callback = new ProcessCallback(mServiceMessenger, processName, msg.replyTo);
                        mRemoteCallbackList.add(callback);
                        if (!ElegantUtil.isServiceProcess(callback.processName())) {
                            postStickyValueToNewProcess(callback);
                        }
                        break;
                    case MSG_UNREGISTER:
                        for (ProcessCallback cb : mRemoteCallbackList) {
                            if (cb.processName().equals(processName) && cb.getMessenger() == msg.replyTo) {
                                mRemoteCallbackList.remove(cb);
                            }
                        }
                        break;
                    case MSG_RESET_STICKY:
                        eventWrapper = msg.getData().getParcelable(MultiProcess.MSG_DATA);
                        if (eventWrapper != null) {
                            BusFactory.ready().getSingleExecutorService().execute(() -> {
                                removeEventFromCache(eventWrapper);
                                callbackToOtherProcess(eventWrapper, MultiProcess.MSG_ON_RESET_STICKY);
                            });
                        }
                        break;
                    case MSG_POST_TO_SERVICE:
                        eventWrapper = msg.getData().getParcelable(MultiProcess.MSG_DATA);
                        if (eventWrapper != null) {
                            BusFactory.ready().getSingleExecutorService().execute(() -> {
                                putEventToCache(eventWrapper);
                                callbackToOtherProcess(eventWrapper, MultiProcess.MSG_ON_POST);
                            });
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
     * 服务进程收到事件先保留，作为其他进程的粘性事件缓存
     *
     * @param eventWrapper 消息
     */
    private void putEventToCache(final EventWrapper eventWrapper) {
        ElegantLog.d("Service receive event, add to cache, Event = " + eventWrapper.toString());
        EventDataBase.getInstance().eventDao().insert(DataUtil.convert(eventWrapper));
    }

    /**
     * 服务进程收到事件先保留，作为其他进程的粘性事件缓存
     *
     * @param eventWrapper 消息
     */
    private void removeEventFromCache(final EventWrapper eventWrapper) {
        ElegantLog.d("Service receive event, remove from cache, Event = " + eventWrapper.toString());
        EventDataBase.getInstance().eventDao().delete(DataUtil.convert(eventWrapper));
    }

    /**
     * 转发事件总线
     *
     * @param eventWrapper 发送值
     */
    private void callbackToOtherProcess(EventWrapper eventWrapper, int what) {
        int count = mRemoteCallbackList.size();
        for (int i = 0; i < count; i++) {
            ProcessCallback callback = mRemoteCallbackList.get(i);
            if (callback == null) continue;
            try {
                if (ElegantUtil.isSameProcess(callback.processName(), eventWrapper.processName)) {
                    ElegantLog.d("This is in same process, already posted, Event = " + eventWrapper);
                } else {
                    ElegantLog.d("call back " + what + " to other process : " + callback.processName() + ", Event = " +
                            eventWrapper);
                    callback.call(eventWrapper, what);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 转发 粘性事件到新的进程
     *
     * @param callback 进程回调
     * @throws RemoteException 异常
     */
    private void postStickyValueToNewProcess(final ProcessCallback callback) throws RemoteException {
        BusFactory.ready().getSingleExecutorService().execute(() -> {
            try {
                ElegantLog.d("Post all sticky event to new process : " + callback.processName());
                List<EventBean> caches = EventDataBase.getInstance().eventDao().getAllList();
                for (EventBean item : caches) {
                    callback.call(DataUtil.convert(item), MultiProcess.MSG_ON_POST_STICKY);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
