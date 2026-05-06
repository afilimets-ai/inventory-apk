package com.honeywell;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* JADX INFO: loaded from: classes3.dex */
public interface IExecutor extends IInterface {

    public static abstract class Stub extends Binder implements IExecutor {
        private static final String DESCRIPTOR = "com.honeywell.IExecutor";
        static final int TRANSACTION_execute = 1;
        static final int TRANSACTION_executeAsync = 2;

        private static class Proxy implements IExecutor {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.honeywell.IExecutor
            public Message execute(Message message) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                Parcel parcelObtain2 = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        parcelObtain.writeInt(1);
                        message.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    this.mRemote.transact(1, parcelObtain, parcelObtain2, 0);
                    parcelObtain2.readException();
                    Message messageCreateFromParcel = parcelObtain2.readInt() != 0 ? Message.CREATOR.createFromParcel(parcelObtain2) : null;
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                    return messageCreateFromParcel;
                } catch (Throwable th) {
                    parcelObtain2.recycle();
                    parcelObtain.recycle();
                    throw th;
                }
            }

            @Override // com.honeywell.IExecutor
            public void executeAsync(Message message, IExecutor iExecutor) throws RemoteException {
                Parcel parcelObtain = Parcel.obtain();
                try {
                    parcelObtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (message != null) {
                        parcelObtain.writeInt(1);
                        message.writeToParcel(parcelObtain, 0);
                    } else {
                        parcelObtain.writeInt(0);
                    }
                    parcelObtain.writeStrongBinder(iExecutor != null ? iExecutor.asBinder() : null);
                    this.mRemote.transact(2, parcelObtain, null, 1);
                    parcelObtain.recycle();
                } catch (Throwable th) {
                    parcelObtain.recycle();
                    throw th;
                }
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IExecutor asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface iInterfaceQueryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (iInterfaceQueryLocalInterface == null || !(iInterfaceQueryLocalInterface instanceof IExecutor)) ? new Proxy(iBinder) : (IExecutor) iInterfaceQueryLocalInterface;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1) {
                if (i == 2) {
                    parcel.enforceInterface(DESCRIPTOR);
                    executeAsync(parcel.readInt() != 0 ? Message.CREATOR.createFromParcel(parcel) : null, asInterface(parcel.readStrongBinder()));
                    return true;
                }
                if (i != 1598968902) {
                    return super.onTransact(i, parcel, parcel2, i2);
                }
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
            parcel.enforceInterface(DESCRIPTOR);
            Message messageExecute = execute(parcel.readInt() != 0 ? Message.CREATOR.createFromParcel(parcel) : null);
            parcel2.writeNoException();
            if (messageExecute != null) {
                parcel2.writeInt(1);
                messageExecute.writeToParcel(parcel2, 1);
            } else {
                parcel2.writeInt(0);
            }
            return true;
        }
    }

    Message execute(Message message) throws RemoteException;

    void executeAsync(Message message, IExecutor iExecutor) throws RemoteException;
}
