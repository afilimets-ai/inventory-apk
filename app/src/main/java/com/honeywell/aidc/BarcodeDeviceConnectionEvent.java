package com.honeywell.aidc;

import java.util.EventObject;

/* JADX INFO: loaded from: classes3.dex */
public final class BarcodeDeviceConnectionEvent extends EventObject {
    private static final long serialVersionUID = 1;
    private BarcodeReaderInfo mBarcodeDevice;
    private int mStatus;

    BarcodeDeviceConnectionEvent(Object obj, BarcodeReaderInfo barcodeReaderInfo, int i) {
        super(obj);
        this.mBarcodeDevice = barcodeReaderInfo;
        this.mStatus = i;
    }

    public BarcodeReaderInfo getBarcodeReaderInfo() {
        return this.mBarcodeDevice;
    }

    public int getConnectionStatus() {
        return this.mStatus;
    }
}
