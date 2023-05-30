/*
 * ************************************************************
 * 文件：BusContentProvider.java  模块：ElegantBus.ipc.main  项目：ElegantBus
 * 当前修改时间：2023年06月01日 18:33:18
 * 上次修改时间：2023年06月01日 17:26:50
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc.main
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import cody.bus.db.EventBean;
import cody.bus.db.EventDataBase;

public class BusContentProvider extends ContentProvider {
    //用来存放所有合法的Uri容器
    private static final String AUTHORITY_END = ".BusContentProvider";
    private static final String PATH_CACHES = "/Caches";
    private static final String PATH_CACHE = "/Caches/item";
    public static String mServiceProcessName;
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int CACHES = 1;
    private static final int CACHE = 2;
    private EventDataBase mEventDataBase;
    private Context mContext;
    private Uri mUri;

    public BusContentProvider() {
        mServiceProcessName = ElegantUtil.getProcessName();
        ElegantLog.i("BusContentProvider start. mServiceProcessName : " + mServiceProcessName);
    }

    public static void addUriMatcher(String pkgName) {
        BusContentProvider.sUriMatcher.addURI(pkgName + AUTHORITY_END, PATH_CACHES + "/#", CACHES);
        BusContentProvider.sUriMatcher.addURI(pkgName + AUTHORITY_END, PATH_CACHE + "/#", CACHE);
    }

    @Override
    public boolean onCreate() {
        ElegantLog.i("BusContentProvider onCreate.");
        mContext = getContext();
        mUri = new Uri.Builder().scheme("content")
                .authority(ElegantUtil.getHostPackageName(mContext) + AUTHORITY_END)
                .path(PATH_CACHES)
                .build();
        if (mContext != null) {
            mEventDataBase =
                    Room.databaseBuilder(mContext, EventDataBase.class,
                                    ElegantUtil.getHostPackageName(mContext) + ".db")
                            .build();
        } else {
            ElegantLog.e("BusContentProvider onCreate.");
        }
        return true;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (TextUtils.isDigitsOnly(method)) {
            int what = Integer.parseInt(method);
            ElegantLog.d("BusContentProvider call. what : " + what + ", arg " + arg);
            switch (what) {
                case MultiProcess.MSG_ON_POST:
                    if (!TextUtils.isEmpty(arg)) {
                        if (extras != null) {
                            extras.setClassLoader(getClass().getClassLoader());
                            EventWrapper eventWrapper = extras.getParcelable(MultiProcess.MSG_DATA);
                            assert eventWrapper != null;
                            mEventDataBase.eventDao().insert(DataUtil.convert(eventWrapper));
                            notifyChange(arg, what);
                        }
                    }
                    break;
                case MultiProcess.MSG_ON_POST_STICKY:
                    mContext.getContentResolver().notifyChange(ContentUris.withAppendedId(mUri, what), null);
                    break;
                case MultiProcess.MSG_ON_RESET_STICKY:
                    if (!TextUtils.isEmpty(arg)) {
                        if (extras != null) {
                            extras.setClassLoader(getClass().getClassLoader());
                            EventWrapper eventWrapper = extras.getParcelable(MultiProcess.MSG_DATA);
                            assert eventWrapper != null;
                            EventBean bean = DataUtil.convert(eventWrapper);
                            bean.valid = false;
                            mEventDataBase.eventDao().update(bean);
                            notifyChange(arg, what);
                        }
                    }
                    break;
            }
        }
        return super.call(method, arg, extras);
    }

    private void notifyChange(String key, int what) {
        ElegantLog.d("BusContentProvider notifyChange. key : " + key + ", what " + what);
        Uri uri = new Uri.Builder().scheme("content")
                .authority(ElegantUtil.getHostPackageName(mContext) + AUTHORITY_END)
                .path(PATH_CACHE)
                .appendQueryParameter(MultiProcess.MSG_DATA, key)
                .build();
        mContext.getContentResolver().notifyChange(ContentUris.withAppendedId(uri, what), null);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
            @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        ElegantLog.d("BusContentProvider query. uri : " + uri + ", selection " + selection);
        int match = sUriMatcher.match(uri);
        ElegantLog.d("BusContentProvider query. match : " + match + ",uri : " + uri);
        if (match == CACHES) {
            return mEventDataBase.eventDao().getAllCursor();
        }
        if (match == CACHE) {
            selection = uri.getQueryParameter(MultiProcess.MSG_DATA);
            return mEventDataBase.eventDao().getByKey(selection);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CACHE:
                return "vnd.android.cursor.item";
            case CACHES:
                return "vnd.android.cursor.dir";
            default:
                throw new IllegalArgumentException("UnKnown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        ElegantLog.w("BusContentProvider insert. uri : " + uri + ", values " + values);
        if (sUriMatcher.match(uri) == CACHE && values != null) {
            ElegantLog.w("BusContentProvider insert match. uri : " + uri + ", values " + values);
            EventBean bean = DataUtil.convert(values);
            mEventDataBase.eventDao().insert(bean);
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        ElegantLog.w("BusContentProvider delete. uri : " + uri + ", selection " + selection);
        if (sUriMatcher.match(uri) == CACHE && selection != null) {
            ElegantLog.w("BusContentProvider delete match. uri : " + uri + ", selection " + selection);
            EventBean bean = new EventBean();
            bean.key = selection;
            return mEventDataBase.eventDao().delete(bean);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
            @Nullable String[] selectionArgs) {
        return 0;
    }
}
