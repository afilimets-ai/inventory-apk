package com.honeywell.aidc;

import android.graphics.Bitmap;

/* JADX INFO: loaded from: classes3.dex */
public class Signature {
    public static final String GUIDANCE_MOVE_DOWN = "moveDown";
    public static final String GUIDANCE_MOVE_LEFT = "moveLeft";
    public static final String GUIDANCE_MOVE_OUT = "moveOut";
    public static final String GUIDANCE_MOVE_RIGHT = "moveRight";
    public static final String GUIDANCE_MOVE_UP = "moveUp";
    public static final String GUIDANCE_SUCCESS = "success";
    public static final String GUIDANCE_UNSUPPORTED_SYMBOLOGY = "unsupportedSymbology";
    private String mGuidance;
    private Bitmap mImage;

    Signature(String str, Bitmap bitmap) {
        this.mImage = bitmap;
        this.mGuidance = str;
    }

    public String getGuidance() {
        return this.mGuidance;
    }

    public Bitmap getImage() {
        return this.mImage;
    }
}
