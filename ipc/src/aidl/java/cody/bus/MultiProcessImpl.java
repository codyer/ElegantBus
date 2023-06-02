/*
 * ************************************************************
 * 文件：MultiProcessImpl.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年06月02日 16:58:02
 * 上次修改时间：2023年06月02日 16:57:16
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * 支持进程间事件总线的扩展，每个进程有一个实例 aidl 实现
 */
public class MultiProcessImpl extends IProcessCallback.Stub implements MultiProcess {
    private boolean mIsBound;
    private String mPkgName;
    private Context mContext;
    private IProcessManager mProcessManager;

    MultiProcessImpl() {
    }

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用 多应用且多进程场景请使用
     *
     * @param context 上下文
     */
    @Override
    public void support(Context context) {
        mContext = context;
        mPkgName = ElegantUtil.getHostPackageName(context);
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
    public <T> void postToProcessManager(EventWrapper eventWrapper, T value) {
        try {
            if (isBind()) {
                mProcessManager.postToProcessManager(ElegantUtil.encode(eventWrapper, value));
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

    @Override
    public String processName() {
        return ElegantUtil.getProcessName();
    }

    @Override
    public void call(final EventWrapper eventWrapper, final int what) {
        BusFactory.ready().getSingleExecutorService().execute(() -> {
            ElegantUtil.decode(eventWrapper, what);
        });
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
     * 为什么不直接使用如下方式bind，为什么一定要有主APP？ { Intent intent = new Intent(mContext,ProcessManagerService.class); mIsBound =
     * mContext.bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE); } 通过mContext方式生成的 Service
     * 每个进程独立，会造成无法实现多APP场景，多App需要绑定到同一个Service上， 因此需要一个主App，来承担 Service 生成的角色
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
                    mProcessManager.unregister(MultiProcessImpl.this);
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

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mProcessManager = IProcessManager.Stub.asInterface(service);
            if (mProcessManager == null) {
                return;
            }
            try {
                service.linkToDeath(mDeathRecipient, 0);
                mProcessManager.register(MultiProcessImpl.this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mProcessManager = null;
            ElegantLog.d("onServiceDisconnected, process = " + processName());
        }
    };
}
