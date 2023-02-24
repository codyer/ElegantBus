/*
 * ************************************************************
 * 文件：IProcessManager.java  模块：ElegantBus.ipc  项目：ElegantBus
 * 当前修改时间：2023年02月24日 17:46:20
 * 上次修改时间：2023年01月05日 14:27:06
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

public interface IProcessManager extends IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    abstract class Stub extends Binder implements IProcessManager {
        private static final String DESCRIPTOR = "cody.bus.IProcessManager";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an cody.bus.IProcessManager interface,
         * generating a proxy if needed.
         */
        public static IProcessManager asInterface(IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin instanceof IProcessManager) {
                return (IProcessManager) iin;
            }
            return new IProcessManager.Stub.Proxy(obj);
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
                case TRANSACTION_register: {
                    data.enforceInterface(descriptor);
                    IProcessCallback callback;
                    callback = IProcessCallback.Stub.asInterface(data.readStrongBinder());
                    this.register(callback);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_unregister: {
                    data.enforceInterface(descriptor);
                    IProcessCallback callback;
                    callback = IProcessCallback.Stub.asInterface(data.readStrongBinder());
                    this.unregister(callback);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_resetSticky: {
                    data.enforceInterface(descriptor);
                    EventWrapper eventWrapper;
                    if ((0 != data.readInt())) {
                        eventWrapper = EventWrapper.CREATOR.createFromParcel(data);
                    } else {
                        eventWrapper = null;
                    }
                    this.resetSticky(eventWrapper);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_postToService: {
                    data.enforceInterface(descriptor);
                    EventWrapper eventWrapper;
                    if ((0 != data.readInt())) {
                        eventWrapper = EventWrapper.CREATOR.createFromParcel(data);
                    } else {
                        eventWrapper = null;
                    }
                    this.postToService(eventWrapper);
                    reply.writeNoException();
                    return true;
                }
                default: {
                    return super.onTransact(code, data, reply, flags);
                }
            }
        }

        private static class Proxy implements IProcessManager {
            private final IBinder mRemote;

            Proxy(IBinder remote) {
                mRemote = remote;
            }

            @Override
            public IBinder asBinder() {
                return mRemote;
            }

            @Override
            public void register(IProcessCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((callback != null)) ? (callback.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_register, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void unregister(IProcessCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((callback != null)) ? (callback.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_unregister, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void resetSticky(EventWrapper eventWrapper) throws RemoteException {
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
                    mRemote.transact(Stub.TRANSACTION_resetSticky, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void postToService(EventWrapper eventWrapper) throws RemoteException {
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
                    mRemote.transact(Stub.TRANSACTION_postToService, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_register = IBinder.FIRST_CALL_TRANSACTION;
        static final int TRANSACTION_unregister = IBinder.FIRST_CALL_TRANSACTION + 1;
        static final int TRANSACTION_resetSticky = IBinder.FIRST_CALL_TRANSACTION + 2;
        static final int TRANSACTION_postToService = IBinder.FIRST_CALL_TRANSACTION + 3;
    }

    void register(IProcessCallback callback) throws RemoteException;

    void unregister(IProcessCallback callback) throws RemoteException;

    void resetSticky(EventWrapper eventWrapper) throws RemoteException;

    void postToService(EventWrapper eventWrapper) throws RemoteException;
}
