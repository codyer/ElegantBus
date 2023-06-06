/*
 * ************************************************************
 * 文件：ProcessManager.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年06月06日 11:07:31
 * 上次修改时间：2023年06月06日 10:40:09
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.List;

import cody.bus.db.EventBean;
import cody.bus.db.EventDataBase;

/**
 * 多进程共享一个实例
 */
public class ProcessManager extends IProcessManager.Stub {
    private final RemoteCallbackList<IProcessCallback> mRemoteCallbackList = new RemoteCallbackList<>();

    public static MultiProcess ready() {
        if (BusFactory.getDelegate() == null) {
            BusFactory.setDelegate(new MultiProcessImpl());
        }
        return BusFactory.getDelegate();
    }

    public ProcessManager() {
        ElegantLog.d("ProcessManager is initialized.");
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
    public void resetSticky(final EventWrapper eventWrapper) {
        BusFactory.ready().getSingleExecutorService().execute(() -> {
            removeEventFromCache(eventWrapper);
            callbackToOtherProcess(eventWrapper, MultiProcess.MSG_ON_RESET_STICKY);
        });
    }

    @Override
    public void postToProcessManager(final EventWrapper eventWrapper) {
        BusFactory.ready().getSingleExecutorService().execute(() -> {
            putEventToCache(eventWrapper);
            callbackToOtherProcess(eventWrapper, MultiProcess.MSG_ON_POST);
        });
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
        int count = mRemoteCallbackList.beginBroadcast();
        try {
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
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mRemoteCallbackList.finishBroadcast();
    }

    /**
     * 转发 粘性事件到新的进程
     *
     * @param callback 进程回调
     */
    private void postStickyValueToNewProcess(final IProcessCallback callback) {
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
