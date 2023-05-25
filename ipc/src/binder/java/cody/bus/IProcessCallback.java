/*
 * ************************************************************
 * 文件：IProcessCallback.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年05月25日 12:34:48
 * 上次修改时间：2023年05月25日 12:31:15
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：ElegantBus.ipc
 * Copyright (c) 2023
 * ************************************************************
 */

package cody.bus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IProcessCallback extends IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public abstract class Stub extends Binder implements IProcessCallback {
        private static final String DESCRIPTOR = "cody.bus.IProcessCallback";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an IProcessCallback interface,
         * generating a proxy if needed.
         */
        public static IProcessCallback asInterface(IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin instanceof IProcessCallback) {
                return (IProcessCallback) iin;
            }
            return new IProcessCallback.Stub.Proxy(obj);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(descriptor);
                    return true;
                }
                case TRANSACTION_processName: {
                    data.enforceInterface(descriptor);
                    String processName = this.processName();
                    reply.writeNoException();
                    reply.writeString(processName);
                    return true;
                }
                case TRANSACTION_call: {
                    data.enforceInterface(descriptor);
                    EventWrapper eventWrapper;
                    if ((0 != data.readInt())) {
                        eventWrapper = EventWrapper.CREATOR.createFromParcel(data);
                    } else {
                        eventWrapper = null;
                    }
                    int what;
                    what = data.readInt();
                    this.call(eventWrapper, what);
                    reply.writeNoException();
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

        private static class Proxy implements IProcessCallback {
            private final IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            @Override
            public String processName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                String _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_processName, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void call(EventWrapper eventWrapper, int what) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((eventWrapper != null)) {
                        _data.writeInt(1);
                        eventWrapper.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(what);
                    mRemote.transact(Stub.TRANSACTION_call, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_processName = IBinder.FIRST_CALL_TRANSACTION;
        static final int TRANSACTION_call = IBinder.FIRST_CALL_TRANSACTION + 1;
    }

    String processName() throws RemoteException;

    void call(EventWrapper eventWrapper, int what) throws RemoteException;
}
