package com.honeywell.aidc;

import android.graphics.Point;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public final class BarcodeReadEvent extends EventObject {
    private static final int NUM_CORNERS = 4;
    private static final long serialVersionUID = 1;
    private String mAimId;
    private List<Point> mBounds;
    private String mCharset;
    private String mCodeId;
    private String mData;
    private String mTimestamp;

    BarcodeReadEvent(Object obj, String str, String str2, String str3, String str4, String str5, String str6) {
        super(obj);
        DebugLog.d("Enter constructor");
        DebugLog.d("data = " + str);
        DebugLog.d("charset = " + str2);
        DebugLog.d("codeid = " + str3);
        DebugLog.d("aimid = " + str4);
        DebugLog.d("timestamp = " + str5);
        this.mData = str;
        this.mCharset = str2;
        this.mCodeId = str3;
        this.mAimId = str4;
        this.mTimestamp = str5;
        this.mBounds = parseBounds(str6);
        DebugLog.d("Exit constructor");
    }

    private List<Point> parseBounds(String str) {
        if (str == null) {
            return null;
        }
        String[] strArrSplit = str.split(";");
        if (strArrSplit.length != 4) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < 4; i++) {
            String[] strArrSplit2 = strArrSplit[i].split(",");
            if (strArrSplit2.length == 2) {
                try {
                    arrayList.add(new Point(Integer.parseInt(strArrSplit2[0]), Integer.parseInt(strArrSplit2[1])));
                } catch (NumberFormatException unused) {
                    DebugLog.d("Could Not Parse Barcode Bounds " + strArrSplit2[0] + "," + strArrSplit2[1]);
                }
            }
        }
        if (arrayList.size() != 4) {
            return null;
        }
        return arrayList;
    }

    public String getAimId() {
        return this.mAimId;
    }

    public List<Point> getBarcodeBounds() {
        return this.mBounds;
    }

    public String getBarcodeData() {
        return this.mData;
    }

    public Charset getCharset() {
        return Charset.forName(this.mCharset);
    }

    public String getCodeId() {
        return this.mCodeId;
    }

    public String getTimestamp() {
        return this.mTimestamp;
    }
}
