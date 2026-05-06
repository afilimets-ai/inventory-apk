package com.honeywell.aidc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.honeywell.IExecutor;
import com.honeywell.Message;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: classes.dex */
public final class AidcManager {
    public static final int BARCODE_DEVICE_CONNECTED = 1;
    public static final int BARCODE_DEVICE_DISCONNECTED = 0;
    private ServiceConnection mConnection;
    private Context mContext;
    private IExecutor mService;
    private static Map<Class<?>, Map<Object, IExecutor>> sListeners = new HashMap();
    private static Map<Class<?>, Map<Object, Integer>> sListenerCounts = new HashMap();

    /* JADX INFO: loaded from: classes3.dex */
    public interface BarcodeDeviceListener extends EventListener {
        void onBarcodeDeviceConnectionEvent(BarcodeDeviceConnectionEvent barcodeDeviceConnectionEvent);
    }

    public interface CreatedCallback {
        void onCreated(AidcManager aidcManager);
    }

    static {
        sListeners.put(BarcodeDeviceListener.class, new HashMap());
        sListenerCounts.put(BarcodeDeviceListener.class, new HashMap());
    }

    AidcManager(Context context, ServiceConnection serviceConnection, IExecutor iExecutor) {
        DebugLog.d("Enter AidcManager constructor");
        this.mContext = context;
        this.mConnection = serviceConnection;
        this.mService = iExecutor;
        DebugLog.d("Exit AidcManager constructor");
    }

    private void addListener(final Object obj, final Class<?> cls) {
        IExecutor iExecutor;
        synchronized (sListeners) {
            try {
                Map<Object, IExecutor> map = sListeners.get(cls);
                iExecutor = null;
                if (map != null) {
                    IExecutor iExecutor2 = map.get(obj);
                    if (iExecutor2 == null) {
                        iExecutor = new IExecutor.Stub() { // from class: com.honeywell.aidc.AidcManager.1
                            @Override // com.honeywell.IExecutor
                            public Message execute(Message message) {
                                EventObject event = DcsJsonRpcHelper.getEvent(AidcManager.this, message);
                                if (!(event instanceof BarcodeDeviceConnectionEvent) || !BarcodeDeviceListener.class.equals(cls)) {
                                    return null;
                                }
                                ((BarcodeDeviceListener) obj).onBarcodeDeviceConnectionEvent((BarcodeDeviceConnectionEvent) event);
                                return null;
                            }

                            @Override // com.honeywell.IExecutor
                            public void executeAsync(Message message, IExecutor iExecutor3) {
                                execute(message);
                            }
                        };
                        incrementListeners(cls, obj, iExecutor);
                    } else {
                        incrementListeners(cls, obj, null);
                        iExecutor = iExecutor2;
                    }
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        Message messageBuild = DcsJsonRpcHelper.build("internal.addListener");
        messageBuild.extras.put("listener", iExecutor);
        DcsJsonRpcHelper.checkRuntimeError(execute(messageBuild));
    }

    public static void create(Context context, final CreatedCallback createdCallback) {
        DebugLog.d("Enter AidcManager.create()");
        if (context == null || createdCallback == null) {
            throw new IllegalArgumentException("The parameters cannot be null.");
        }
        final Context applicationContext = context.getApplicationContext();
        applicationContext.bindService(new Intent("com.honeywell.decode.DecodeService").setComponent(new ComponentName("com.intermec.datacollectionservice", "com.intermec.datacollectionservice.DataCollectionService")), new ServiceConnection() { // from class: com.honeywell.aidc.AidcManager.2
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                DebugLog.d("Enter onServiceConnected");
                createdCallback.onCreated(new AidcManager(applicationContext, this, IExecutor.Stub.asInterface(iBinder)));
                DebugLog.d("Exit onServiceConnected");
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                DebugLog.d("Enter onServiceDisconnected");
                DebugLog.d("Exit onServiceDisconnected");
            }
        }, 1);
        DebugLog.d("Exit AidcManager constructor");
    }

    private void decrementListeners(Class<?> cls, Object obj) {
        Map<Object, Integer> map = sListenerCounts.get(cls);
        if (map != null) {
            int iIntValue = map.get(obj).intValue();
            if (iIntValue != 1) {
                map.put(obj, Integer.valueOf(iIntValue - 1));
                return;
            }
            Map<Object, IExecutor> map2 = sListeners.get(cls);
            if (map2 != null) {
                map2.remove(obj);
                map.remove(obj);
            }
        }
    }

    private void incrementListeners(Class<?> cls, Object obj, IExecutor iExecutor) {
        Map<Object, Integer> map = sListenerCounts.get(cls);
        if (map != null) {
            if (iExecutor == null) {
                map.put(obj, Integer.valueOf(map.get(obj).intValue() + 1));
                return;
            }
            Map<Object, IExecutor> map2 = sListeners.get(cls);
            if (map2 != null) {
                map2.put(obj, iExecutor);
                map.put(obj, 1);
            }
        }
    }

    private void removeListener(Object obj, Class<?> cls) {
        synchronized (sListeners) {
            try {
                IExecutor iExecutor = sListeners.get(cls).get(obj);
                if (iExecutor == null) {
                    return;
                }
                decrementListeners(cls, obj);
                Message messageBuild = DcsJsonRpcHelper.build("internal.removeListener");
                messageBuild.extras.put("listener", iExecutor);
                DcsJsonRpcHelper.checkRuntimeError(execute(messageBuild));
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void addBarcodeDeviceListener(BarcodeDeviceListener barcodeDeviceListener) {
        addListener(barcodeDeviceListener, BarcodeDeviceListener.class);
    }

    public void close() {
        DebugLog.d("Enter AidcManager.close()");
        ServiceConnection serviceConnection = this.mConnection;
        if (serviceConnection != null) {
            this.mContext.unbindService(serviceConnection);
            this.mConnection = null;
        }
        DebugLog.d("Exit AidcManager.close()");
    }

    public BarcodeReader createBarcodeReader() {
        List<BarcodeReaderInfo> listListConnectedBarcodeDevices = listConnectedBarcodeDevices();
        int size = listListConnectedBarcodeDevices.size();
        for (int i = 0; i < size; i++) {
            if (listListConnectedBarcodeDevices.get(i).getName().equals("dcs.scanner.imager")) {
                return createBarcodeReader("dcs.scanner.imager");
            }
            if (listListConnectedBarcodeDevices.get(i).getName().equals("dcs.scanner.serial1")) {
                return createBarcodeReader("dcs.scanner.serial1");
            }
        }
        return null;
    }

    public Message execute(Message message) {
        try {
            return this.mService.execute(message);
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to execute request", e);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public Context getContext() {
        return this.mContext;
    }

    public List<BarcodeReaderInfo> listBarcodeDevices() {
        ArrayList arrayList = new ArrayList();
        try {
            Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.listScanners"));
            try {
                DcsJsonRpcHelper.checkRuntimeError(messageExecute);
                JSONArray jSONArray = new JSONObject(messageExecute.action).getJSONObject("result").getJSONArray("scanners");
                for (int i = 0; i < jSONArray.length(); i++) {
                    arrayList.add(DcsJsonRpcHelper.buildBarcodeReaderInfo(jSONArray.getJSONObject(i)));
                }
                return arrayList;
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Method not found")) {
                    DebugLog.d("This method is not supported for the installed DCS version.");
                    return null;
                }
                if (!e.getMessage().contains("Action is null")) {
                    throw e;
                }
                DebugLog.d("Message action is null.");
                return null;
            }
        } catch (JSONException e2) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e2);
        }
    }

    public List<BarcodeReaderInfo> listConnectedBarcodeDevices() {
        ArrayList arrayList = new ArrayList();
        try {
            Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.listConnectedScanners"));
            try {
                DcsJsonRpcHelper.checkRuntimeError(messageExecute);
                JSONArray jSONArray = new JSONObject(messageExecute.action).getJSONObject("result").getJSONArray("scanners");
                for (int i = 0; i < jSONArray.length(); i++) {
                    arrayList.add(DcsJsonRpcHelper.buildBarcodeReaderInfo(jSONArray.getJSONObject(i)));
                }
                return arrayList;
            } catch (RuntimeException e) {
                if (!e.getMessage().contains("Method not found")) {
                    throw e;
                }
                DebugLog.d("This method is not supported for the installed DCS version.");
                return null;
            }
        } catch (JSONException e2) {
            throw new RuntimeException("An error occurred while communicating with the scanner service.", e2);
        }
    }

    public void removeBarcodeDeviceListener(BarcodeDeviceListener barcodeDeviceListener) {
        removeListener(barcodeDeviceListener, BarcodeDeviceListener.class);
    }

    public BarcodeReader createBarcodeReader(String str) throws InvalidScannerNameException {
        Message messageExecute = execute(DcsJsonRpcHelper.build("scanner.connect", "scanner", str));
        DcsJsonRpcHelper.checkRuntimeAndScannerExceptions(messageExecute);
        return new BarcodeReader(IExecutor.Stub.asInterface((IBinder) messageExecute.extras.get("session")));
    }
}
