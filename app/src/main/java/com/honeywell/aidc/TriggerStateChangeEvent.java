package com.honeywell.aidc;

import java.util.EventObject;

/* JADX INFO: loaded from: classes.dex */
public final class TriggerStateChangeEvent extends EventObject {
    private static final long serialVersionUID = 1;
    private boolean mState;

    TriggerStateChangeEvent(Object obj, boolean z) {
        super(obj);
        DebugLog.d("Enter constructor");
        StringBuilder sb = new StringBuilder();
        sb.append("state = ");
        sb.append(z ? "pressed" : "released");
        DebugLog.d(sb.toString());
        this.mState = z;
        DebugLog.d("Exit constructor");
    }

    public boolean getState() {
        return this.mState;
    }
}
