/*
 * ************************************************************
 * 文件：IProcessManager.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 14:28:20
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.RemoteException;

public interface IProcessManager {
    void register() throws RemoteException;

    void unregister();

    void resetSticky(EventWrapper eventWrapper) throws RemoteException;

    void postToProcessManager(EventWrapper eventWrapper) throws RemoteException;
}
