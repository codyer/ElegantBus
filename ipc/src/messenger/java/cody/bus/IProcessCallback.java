/*
 * ************************************************************
 * 文件：IProcessCallback.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 17:08:39
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.RemoteException;

interface IProcessCallback {

    String processName() throws RemoteException;

    void call(EventWrapper eventWrapper, int what) throws RemoteException;
}