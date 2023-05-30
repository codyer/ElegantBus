/*
 * ************************************************************
 * 文件：ProcessCallback.java  模块：ElegantBus.ipc.main  项目：ElegantBus
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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ProcessCallback implements IProcessCallback {
    private final Messenger mServiceMessenger;
    private final String mProcessName;
    private final Messenger messenger;

    public ProcessCallback(Messenger serviceMessenger, String processName, Messenger messenger) {
        mServiceMessenger = serviceMessenger;
        mProcessName = processName;
        this.messenger = messenger;
    }

    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public String processName() throws RemoteException {
        return mProcessName;
    }

    @Override
    public void call(final EventWrapper eventWrapper, final int what) throws RemoteException {
        Message message = Message.obtain(null, what);
        message.replyTo = mServiceMessenger;
        Bundle data = new Bundle();
        data.putParcelable(MultiProcess.MSG_DATA, eventWrapper);
        message.setData(data);
        messenger.send(message);
    }
}
