/*
 * ************************************************************
 * 文件：IProcessManager.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 11:29:01
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.RemoteException;

interface IProcessManager {
    void register(IProcessCallback processCallback) throws RemoteException;

    void unregister(IProcessCallback processCallback) throws RemoteException;

    void resetSticky(EventWrapper eventWrapper) throws RemoteException;

    void postToProcessManager(EventWrapper eventWrapper) throws RemoteException;
}
