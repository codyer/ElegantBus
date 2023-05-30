/*
 * ************************************************************
 * 文件：ProcessManager.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年06月01日 17:08:51
 * 上次修改时间：2023年06月01日 17:07:49
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;

import androidx.annotation.Nullable;

/**
 * 实际是个Proxy
 */
public class ProcessManager extends ContentObserver implements IProcessManager {
    public static MultiProcess ready() {
        if (BusFactory.getDelegate() == null) {
            BusFactory.setDelegate(new MultiProcessImpl());
        }
        return BusFactory.getDelegate();
    }

    private final Context mContext;
    private final Uri mUri;
    private final ContentProviderClient mContentProviderClient;

    public ProcessManager(Context context) {
        super(new Handler());
        mContext = context;
        mUri = new Uri.Builder().scheme("content")
                .authority(ElegantUtil.getHostPackageName(context) + ".BusContentProvider")
                .build();
        mContentProviderClient = context.getContentResolver().acquireContentProviderClient(mUri);
    }

    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri) {
        super.onChange(selfChange, uri);
        ElegantLog.d("onChange : mProcessName : " + ElegantUtil.getProcessName() + ", selfChange : " + selfChange +
                ", uri : " + uri);
        if (uri != null) {
            int what = (int) ContentUris.parseId(uri);
            Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    EventWrapper eventWrapper = DataUtil.convert(cursor);
                    if (ElegantUtil.isSameProcess(ElegantUtil.getProcessName(), eventWrapper.processName) &&
                            what != MultiProcess.MSG_ON_POST_STICKY) {
                        ElegantLog.d("This is in same process, already posted, Event = " + eventWrapper);
                    } else {
                        ElegantLog.d("call back " + what + " to other process : " + ElegantUtil.getProcessName() +
                                ", Event = " +
                                eventWrapper);
                        ElegantUtil.decode(eventWrapper, what);
                    }
                }
            }
        }
    }

    @Override
    public void register() throws RemoteException {
        ElegantLog.d("register mProcessName : " + ElegantUtil.getProcessName() + ",mUri : " + mUri);
        if (ElegantUtil.isServiceProcess(ElegantUtil.getProcessName())) {
            ElegantLog.d("register isServiceProcess");
            return;
        }
        mContext.getContentResolver().registerContentObserver(mUri, true, this);
        mContentProviderClient.call(String.valueOf(MultiProcess.MSG_ON_POST_STICKY), mUri.toString(), null);
    }

    @Override
    public void unregister() {
        ElegantLog.d("unregister mProcessName : " + ElegantUtil.getProcessName());
        if (ElegantUtil.isServiceProcess(ElegantUtil.getProcessName())) {
            ElegantLog.d("unregister isServiceProcess");
            return;
        }
        mContext.getContentResolver().unregisterContentObserver(this);
        mContentProviderClient.release();
    }

    @Override
    public void resetSticky(EventWrapper eventWrapper) throws RemoteException {
        ElegantLog.d(
                "resetSticky mProcessName : " + ElegantUtil.getProcessName() + ", eventWrapper : " + eventWrapper);
        if (ElegantUtil.isServiceProcess(ElegantUtil.getProcessName())) {
            ElegantLog.d("resetSticky isServiceProcess");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(MultiProcess.MSG_DATA, eventWrapper);
        mContentProviderClient.call(String.valueOf(MultiProcess.MSG_ON_RESET_STICKY), eventWrapper.getKey(), bundle);
    }

    @Override
    public void postToProcessManager(EventWrapper eventWrapper) throws RemoteException {
        ElegantLog.d("postToProcessManager mProcessName : " + ElegantUtil.getProcessName() + ", eventWrapper : " +
                eventWrapper);
        if (ElegantUtil.isServiceProcess(ElegantUtil.getProcessName())) {
            ElegantLog.d("postToProcessManager isServiceProcess");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(MultiProcess.MSG_DATA, eventWrapper);
        mContentProviderClient.call(String.valueOf(MultiProcess.MSG_ON_POST), eventWrapper.getKey(), bundle);
    }
}
