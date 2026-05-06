package com.honeywell.aidc;

import java.util.EventObject;

/* JADX INFO: loaded from: classes.dex */
public final class BarcodeFailureEvent extends EventObject {
    private static final long serialVersionUID = 1;
    private String mTimestamp;

    BarcodeFailureEvent(Object obj, String str) {
        super(obj);
        DebugLog.d("Enter constructor");
        DebugLog.d("timestamp = " + str);
        this.mTimestamp = str;
        DebugLog.d("Exit constructor");
    }

    public String getTimestamp() {
        return this.mTimestamp;
    }
}
