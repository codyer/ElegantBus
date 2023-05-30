/*
 * ************************************************************
 * 文件：ProcessManager.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 14:10:34
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ProcessManager extends Handler implements IProcessManager {
    Messenger mServiceMessenger;
    Messenger mProcessMessenger;
    IProcessCallback mProcessCallback;

    public static MultiProcess ready() {
        if (BusFactory.getDelegate() == null) {
            BusFactory.setDelegate(new MultiProcessImpl());
        }
        return BusFactory.getDelegate();
    }

    ProcessManager(final IBinder serviceMessenger) {
        super();
        mServiceMessenger = new Messenger(serviceMessenger);
        mProcessMessenger = new Messenger(this);
    }

    IBinder asBinder() {
        return mServiceMessenger.getBinder();
    }

    @Override
    public void handleMessage(Message msg) {
        // fix BadParcelableException: ClassNotFoundException when unmarshalling
        msg.getData().setClassLoader(getClass().getClassLoader());
        EventWrapper eventWrapper = msg.getData().getParcelable(MultiProcess.MSG_DATA);
        if (eventWrapper != null && mProcessCallback != null) {
            try {
                mProcessCallback.call(eventWrapper, msg.what);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
        super.handleMessage(msg);
    }

    @Override
    public void register(IProcessCallback processCallback) throws RemoteException {
        mProcessCallback = processCallback;
        sendWithName(ElegantBusService.MSG_REGISTER);
    }

    @Override
    public void unregister(IProcessCallback processCallback) throws RemoteException {
        sendWithName(ElegantBusService.MSG_UNREGISTER);
    }

    @Override
    public void resetSticky(final EventWrapper eventWrapper) throws RemoteException {
        send(eventWrapper, ElegantBusService.MSG_RESET_STICKY);
    }

    @Override
    public void postToProcessManager(final EventWrapper eventWrapper) throws RemoteException {
        send(eventWrapper, ElegantBusService.MSG_POST_TO_SERVICE);
    }

    private void sendWithName(final int msg) throws RemoteException {
        Bundle data = new Bundle();
        data.putString(ElegantBusService.MSG_PROCESS_NAME, mProcessCallback.processName());
        send(msg, data);
    }

    private void send(final EventWrapper eventWrapper, final int msgPostToService) throws RemoteException {
        Bundle data = new Bundle();
        data.putParcelable(MultiProcess.MSG_DATA, eventWrapper);
        send(msgPostToService, data);
    }

    private void send(final int what, Bundle data) throws RemoteException {
        Message message = Message.obtain(null, what);
        message.replyTo = mProcessMessenger;
        message.setData(data);
        mServiceMessenger.send(message);
    }
}
