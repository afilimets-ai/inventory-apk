package com.honeywell.aidc;

/* JADX INFO: loaded from: classes3.dex */
public class SignatureParameters {
    private int mAspectRatio;
    private boolean mBinarized;
    private int mHeight;
    private int mHorizontalOffset;
    private int mResolution;
    private int mVerticalOffset;
    private int mWidth;

    public SignatureParameters() {
    }

    public int getAspectRatio() {
        return this.mAspectRatio;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getHorizontalOffset() {
        return this.mHorizontalOffset;
    }

    public int getResolution() {
        return this.mResolution;
    }

    public int getVerticalOffset() {
        return this.mVerticalOffset;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public boolean isBinarized() {
        return this.mBinarized;
    }

    public void setAspectRatio(int i) {
        this.mAspectRatio = i;
    }

    public void setBinarized(boolean z) {
        this.mBinarized = z;
    }

    public void setHeight(int i) {
        this.mHeight = i;
    }

    public void setHorizontalOffset(int i) {
        this.mHorizontalOffset = i;
    }

    public void setResolution(int i) {
        this.mResolution = i;
    }

    public void setVerticalOffset(int i) {
        this.mVerticalOffset = i;
    }

    public void setWidth(int i) {
        this.mWidth = i;
    }

    public SignatureParameters(int i, int i2, int i3, int i4, int i5, int i6, boolean z) {
        this.mAspectRatio = i;
        this.mHorizontalOffset = i2;
        this.mVerticalOffset = i3;
        this.mWidth = i4;
        this.mHeight = i5;
        this.mResolution = i6;
        this.mBinarized = z;
    }
}
