/*
 * ************************************************************
 * 文件：IProcessManager.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月05日 20:59:58
 * 上次修改时间：2023年06月05日 20:43:19
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.RemoteException;

public interface IProcessManager {
    boolean register() throws RemoteException;

    void unregister();

    void resetSticky(EventWrapper eventWrapper) throws RemoteException;

    void postToProcessManager(EventWrapper eventWrapper) throws RemoteException;
}
