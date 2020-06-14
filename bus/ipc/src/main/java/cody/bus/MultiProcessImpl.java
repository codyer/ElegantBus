/*
 * ************************************************************
 * 文件：MultiProcessImpl.java  模块：ipc  项目：ElegantBus
 * 当前修改时间：2020年06月15日 00:35:24
 * 上次修改时间：2020年06月15日 00:30:33
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ipc
 * Copyright (c) 2020
 * ************************************************************
 */

package cody.bus;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.alibaba.fastjson.JSON;


/**
 * 支持进程间事件总线的扩展
 */
class MultiProcessImpl implements BusFactory.MultiProcess {
    private String mHostName;
    private final String mProcessName;
    private IBusProcess mBusProcess;
    private IBusListener mBusListener;
    private Context mContext;

    private MultiProcessImpl() {
        mProcessName = Application.getProcessName();
        BusFactory.setDelegate(this);
    }

    private final static class InstanceHolder {
        @SuppressLint("StaticFieldLeak")
        static final MultiProcessImpl INSTANCE = new MultiProcessImpl();
    }

    private static MultiProcessImpl ready() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 进程创建时调用，一般在 Application 的 onCreate 中调用
     * 多应用且多进程场景请使用
     *
     * @param context           上下文
     * @param mainApplicationId 共享服务且常驻的包名
     *                          如果是单应用，即为应用的包名
     *                          如果是多个应用，即为常驻的主应用的包名
     *                          主应用必须安装，否则不能正常运行
     */
    static void support(Context context, String mainApplicationId) {
        ready().mContext = context;
        ready().mHostName = mainApplicationId;
        ready().bindService(mainApplicationId);
    }

    /**
     * 进程结束时调用，一般在 Application 的 onTerminate 中调用
     */
    static void stopSupport() {
        ready().unbindService();
    }

    @Override
    public String hostName() {
        return mHostName;
    }

    @Override
    public <T> void post(String group, String event, String type, T value) {
        try {
            ready().mBusProcess.post(ready().mProcessName, group, event, type, JSON.toJSONString(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBusProcess = IBusProcess.Stub.asInterface(service);
            try {
                mBusProcess.register(mBusListener = new IBusListener.Stub() {
                    @Override
                    public String process() {
                        return mProcessName;
                    }

                    @Override
                    public void onPost(String group, String event, String type, String value) {
                        postToCurrentProcess(group, event, type, value, false);
                        Log.d(ElegantBus.ELEGANT_TAG, "onPost(" + value + ")to process=" + Application.getProcessName());
                    }

                    @Override
                    public void onPostInit(String group, String event, String type, String value) {
                        postToCurrentProcess(group, event, type, value, true);
                        Log.d(ElegantBus.ELEGANT_TAG, "onPostInit(" + value + ")to process=" + Application.getProcessName());
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(ElegantBus.ELEGANT_TAG, "onServiceDisconnected id=" + Thread.currentThread().getName());
        }
    };

    private void postToCurrentProcess(final String group, final String event, final String type, final String value, final boolean init) {
        try {
            Object obj = JSON.parseObject(value, Class.forName(type));
            if (init) {
                BusFactory.ready().create(group, event, type, false).postInitValue(obj);
            } else {
                BusFactory.ready().create(group, event, type, false).postToCurrentProcess(obj);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void bindService(final String mainApplicationId) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(mainApplicationId, MultiProcessService.CLASS_NAME));
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        mContext.unbindService(mServiceConnection);
        if (mBusProcess != null && mBusProcess.asBinder().isBinderAlive()) {
            try {
                // 取消注册
                mBusProcess.unregister(mBusListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mContext = null;
    }
}
