/*
 * ************************************************************
 * 文件：MultiProcessImpl.java  模块：ipc-messenger  项目：ElegantBus
 * 当前修改时间：2020年07月21日 23:29:29
 * 上次修改时间：2020年07月21日 23:29:02
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ipc-messenger
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;


/**
 * 支持进程间事件总线的扩展，每个进程有一个实例
 * messenger 实现
 */
class MultiProcessImpl implements BusFactory.MultiProcess {
    private boolean mIsBound;
    private String mPkgName;
    private Context mContext;
    private final String mProcessName;
    private ProcessManager mProcessManager;

    private MultiProcessImpl() {
        mProcessName = ElegantBus.getProcessName();
    }

    static BusFactory.MultiProcess ready() {
        if (BusFactory.getDelegate() == null) {
            BusFactory.setDelegate(new MultiProcessImpl());
        }
        return BusFactory.getDelegate();
    }

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用
     * 多应用且多进程场景请使用
     *
     * @param context 上下文
     */
    @Override
    public void support(Context context) {
        mContext = context;
        try {
            ComponentName cn = new ComponentName(context, ProcessManagerService.class);
            ServiceInfo info = context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
            boolean supportMultiApp = info.metaData.getBoolean("BUS_SUPPORT_MULTI_APP", false);
            if (!supportMultiApp) {
                mPkgName = context.getPackageName();
            } else {
                String mainApplicationId = info.metaData.getString("BUS_MAIN_APPLICATION_ID");
                if (TextUtils.isEmpty(mainApplicationId)) {
                    ElegantLog.e("\n\nCan not find the host app under :" + pkgName());
                    if (ElegantLog.isDebug()) {
                        throw new RuntimeException("Must config {BUS_MAIN_APPLICATION_ID} in manifestPlaceholders .");
                    }
                    return;
                } else {
                    mPkgName = mainApplicationId;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        bindService();
    }

    /**
     * 进程结束时调用，一般在 Application 的 onTerminate 中调用
     */
    @Override
    public void stopSupport() {
        unbindService();
    }

    @Override
    public String pkgName() {
        return mPkgName;
    }

    @Override
    public <T> void postToService(EventWrapper eventWrapper, T value) {
        try {
            if (mProcessManager == null) {
                bindService();
            } else {
                eventWrapper.json = JSON.toJSONString(value);
                mProcessManager.post(eventWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetSticky(final EventWrapper eventWrapper) {
        try {
            if (mProcessManager == null) {
                bindService();
            } else {
                mProcessManager.resetSticky(eventWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mProcessManager = new ProcessManager(new Messenger(service));
            try {
                mProcessManager.register();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mProcessManager = null;
            ElegantLog.d("onServiceDisconnected, process = " + mProcessName);
        }
    };

    private void bindService() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(pkgName(), ProcessManagerService.CLASS_NAME));
        mIsBound = mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (!mIsBound) {
            ElegantLog.e("\n\nCan not find the host app under :" + pkgName());
            if (ElegantLog.isDebug()) {
                throw new RuntimeException("Can not find the host app under :" + pkgName());
            }
        }
    }

    private void unbindService() {
        if (mIsBound) {
            mContext.unbindService(mServiceConnection);
            if (mProcessManager != null && mProcessManager.asBinder().isBinderAlive()) {
                try {
                    // 取消注册
                    mProcessManager.unregister();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mIsBound = false;
            mContext = null;
        }
    }

    final class ProcessManager {
        Messenger messenger;

        ProcessManager(final Messenger messenger) {
            this.messenger = messenger;
        }

        void post(final EventWrapper eventWrapper) throws RemoteException {
            Message message = Message.obtain(null, ProcessManagerService.MSG_POST_TO_SERVICE);
            message.replyTo = mProcessMessenger;
            Bundle data = new Bundle();
            data.putParcelable(ProcessManagerService.MSG_DATA, eventWrapper);
            message.setData(data);
            messenger.send(message);
        }

        void resetSticky(final EventWrapper eventWrapper) throws RemoteException {
            Message message = Message.obtain(null, ProcessManagerService.MSG_RESET_STICKY);
            message.replyTo = mProcessMessenger;
            Bundle data = new Bundle();
            data.putParcelable(ProcessManagerService.MSG_DATA, eventWrapper);
            message.setData(data);
            messenger.send(message);
        }

        void register() throws RemoteException {
            doRegister(ProcessManagerService.MSG_REGISTER);
        }

        IBinder asBinder() {
            return messenger.getBinder();
        }

        void unregister() throws RemoteException {
            doRegister(ProcessManagerService.MSG_UNREGISTER);
        }

        private void doRegister(final int what) throws RemoteException {
            Message message = Message.obtain(null, what);
            message.replyTo = mProcessMessenger;
            Bundle data = new Bundle();
            data.putString(ProcessManagerService.MSG_PROCESS_NAME, mProcessName);
            message.setData(data);
            messenger.send(message);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Messenger mProcessMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // fix BadParcelableException: ClassNotFoundException when unmarshalling
            msg.getData().setClassLoader(getClass().getClassLoader());
            EventWrapper eventWrapper = msg.getData().getParcelable(ProcessManagerService.MSG_DATA);
            if (eventWrapper != null) {
                switch (msg.what) {
                    case ProcessManagerService.MSG_ON_POST:
                        Object value = null;
                        try {
                            value = JSON.parseObject(eventWrapper.json, Class.forName(eventWrapper.type));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (value == null) return;
                        BusFactory.ready().create(eventWrapper).postToCurrentProcess(value);
                        break;
                    case ProcessManagerService.MSG_ON_RESET_STICKY:
                        BusFactory.ready().create(eventWrapper).resetSticky();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    });
}
