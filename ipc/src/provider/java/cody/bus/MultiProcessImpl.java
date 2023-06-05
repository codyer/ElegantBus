/*
 * ************************************************************
 * 文件：MultiProcessImpl.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月05日 20:59:58
 * 上次修改时间：2023年06月05日 20:43:19
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.Context;
import android.os.RemoteException;

/**
 * 支持进程间事件总线的扩展，每个进程有一个实例 ContentProvider 实现
 */
public class MultiProcessImpl implements MultiProcess {
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
        BusContentProvider.addUriMatcher(mPkgName);
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
            if (isBound()) {
                mProcessManager.postToProcessManager(ElegantUtil.encode(eventWrapper, value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resetSticky(final EventWrapper eventWrapper) {
        try {
            if (isBound()) {
                mProcessManager.resetSticky(eventWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isBound() {
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
        mProcessManager = new ProcessManager(mContext);
        try {
            mIsBound = mProcessManager.register();
        } catch (RemoteException e) {
            e.printStackTrace();
            mIsBound = false;
        }
        if (!mIsBound) {
            mProcessManager.unregister();
            mProcessManager = null;
        }
    }

    private synchronized void unbindService() {
        if (mIsBound) {
            if (mProcessManager != null) {
                mProcessManager.unregister();
            }
            mIsBound = false;
        }
        mContext = null;
    }
}
