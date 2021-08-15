/*
 * ************************************************************
 * 文件：MultiProcessImpl.java  模块：ElegantBus.bus.ipc-aidl  项目：ElegantBus
 * 当前修改时间：2021年08月15日 17:56:45
 * 上次修改时间：2021年08月15日 17:56:26
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.bus.ipc-aidl
 * Copyright (c) 2021
 * ************************************************************
 */

package cody.bus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

/**
 * 支持进程间事件总线的扩展，每个进程有一个实例
 * aidl 实现
 */
class MultiProcessImpl implements MultiProcess {
    private boolean mIsBound;
    private String mPkgName;
    private Context mContext;
    private final String mProcessName;
    private IProcessManager mProcessManager;

    private MultiProcessImpl() {
        mProcessName = ElegantBus.getProcessName();
    }

    static MultiProcess ready() {
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
            ComponentName cn = new ComponentName(context, ElegantBusService.class);
            ServiceInfo info = context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
            boolean supportMultiApp = info.metaData.getBoolean("BUS_SUPPORT_MULTI_APP", false);
            if (!supportMultiApp) {
                mPkgName = context.getPackageName();
            } else {
                String mainApplicationId = info.metaData.getString("BUS_MAIN_APPLICATION_ID");
                if (TextUtils.isEmpty(mainApplicationId)) {
                    ElegantLog.e("Must config {BUS_MAIN_APPLICATION_ID} in manifestPlaceholders .");
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
            if (isBind()) {
                mProcessManager.postToService(MultiProcess.encode(eventWrapper, value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetSticky(final EventWrapper eventWrapper) {
        try {
            if (isBind()) {
                mProcessManager.resetSticky(eventWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isBind() {
        if (mContext == null) {
            return false;
        }
        if (mProcessManager == null) {
            bindService();
        }
        return mIsBound && mProcessManager != null;
    }

    /**
     * 为什么不直接使用如下方式bind，为什么一定要有主APP？
     * {
     * Intent intent = new Intent(mContext,ProcessManagerService.class);
     * mIsBound =  mContext.bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
     * }
     * 通过mContext方式生成的 Service 每个进程独立，会造成无法实现多APP场景，多App需要绑定到同一个Service上，
     * 因此需要一个主App，来承担 Service 生成的角色
     */
    private synchronized void bindService() {
        if (mContext == null) return;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(pkgName(), ElegantBusService.class.getName()));
        mIsBound = mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (!mIsBound) {
            ElegantLog.e("\n\nCan not find the host app under :" + pkgName());
            if (ElegantLog.isDebug()) {
                throw new RuntimeException("Can not find the host app under :" + pkgName());
            }
        }
    }

    private synchronized void unbindService() {
        if (mIsBound) {
            if (mProcessManager != null && mProcessManager.asBinder().isBinderAlive()) {
                try {
                    // 取消注册
                    mProcessManager.unregister(mProcessCallback);
                    mProcessManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mContext.unbindService(mServiceConnection);
            mIsBound = false;
        }
        mContext = null;
    }

    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (mProcessManager == null) {
                return;
            }
            mProcessManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mProcessManager = null;
            bindService();
        }
    };

    private final IProcessCallback mProcessCallback = new IProcessCallback.Stub() {
        @Override
        public String processName() {
            return mProcessName;
        }

        @Override
        public void call(final EventWrapper eventWrapper, final int what) {
            MultiProcess.decode(eventWrapper, what);
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mProcessManager = IProcessManager.Stub.asInterface(service);
            if (mProcessManager == null) {
                return;
            }
            try {
                service.linkToDeath(mDeathRecipient, 0);
                mProcessManager.register(mProcessCallback);
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
}
