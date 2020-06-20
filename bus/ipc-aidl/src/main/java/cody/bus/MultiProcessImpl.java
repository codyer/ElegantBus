/*
 * ************************************************************
 * 文件：MultiProcessImpl.java  模块：ipc-aidl  项目：ElegantBus
 * 当前修改时间：2020年06月20日 17:34:58
 * 上次修改时间：2020年06月20日 17:32:09
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ipc-aidl
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;


/**
 * 支持进程间事件总线的扩展，每个进程有一个实例
 * aidl 实现
 */
class MultiProcessImpl implements BusFactory.MultiProcess {
    private boolean mIsBound;
    private String mPkgName;
    private Context mContext;
    private final String mProcessName;
    private IProcessManager mProcessManager;

    private MultiProcessImpl() {
        mProcessName = Application.getProcessName();
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

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mProcessManager = IProcessManager.Stub.asInterface(service);
            if (mProcessManager == null) return;
            try {
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

    private static void postToCurrentProcess(EventWrapper eventWrapper, final boolean sticky) {
        Object value = null;
        try {
            value = JSON.parseObject(eventWrapper.json, Class.forName(eventWrapper.type));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (value == null) return;
        if (sticky) {
            BusFactory.ready().create(eventWrapper).postStickyToCurrentProcess(value);
        } else {
            BusFactory.ready().create(eventWrapper).postToCurrentProcess(value);
        }
    }

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
                    mProcessManager.unregister(mProcessCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mIsBound = false;
            mContext = null;
        }
    }

    private final IProcessCallback mProcessCallback = new IProcessCallback.Stub() {
        @Override
        public String processName() {
            return mProcessName;
        }

        @Override
        public void onPost(final EventWrapper eventWrapper) {
            postToCurrentProcess(eventWrapper, false);
        }

        @Override
        public void onPostSticky(final EventWrapper eventWrapper) {
            postToCurrentProcess(eventWrapper, true);
        }
    };
}
