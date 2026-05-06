package com.honeywell.aidc;

import java.util.EventObject;

/* JADX INFO: loaded from: classes3.dex */
public final class MenuCommandEvent extends EventObject {
    private static final long serialVersionUID = 1;
    private int mStatus;
    private String m_MenuCommandRes;

    MenuCommandEvent(Object obj, String str, int i) {
        super(obj);
        this.m_MenuCommandRes = str;
        this.mStatus = i;
    }

    public String getMenuCommandRes() {
        return this.m_MenuCommandRes;
    }
}
