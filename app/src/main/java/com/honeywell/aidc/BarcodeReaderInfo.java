package com.honeywell.aidc;

/* JADX INFO: loaded from: classes3.dex */
public final class BarcodeReaderInfo {
    private String mControlLogicVersion;
    private String mFastDecoderVersion;
    private int mFrameHeight;
    private int mFrameWidth;
    private String mFriendlyName;
    private String mFullDecoderVersion;
    private String mName;
    private String mScanEngineFirmwareVersion;
    private String mScanEngineId;
    private String mScanEngineSerialNumber;
    private String mScanEngineVersionNumber;

    BarcodeReaderInfo(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, int i, int i2) {
        this(str, str2, str3, str4, str5, str6, str7, i, i2);
        this.mScanEngineFirmwareVersion = str8;
        this.mScanEngineSerialNumber = str9;
    }

    public String getControlLogicVersion() {
        return this.mControlLogicVersion;
    }

    public String getFastDecodeVersion() {
        return this.mFastDecoderVersion;
    }

    public int getFrameHeight() {
        return this.mFrameHeight;
    }

    public int getFrameWidth() {
        return this.mFrameWidth;
    }

    public String getFriendlyName() {
        return this.mFriendlyName;
    }

    public String getFullDecodeVersion() {
        return this.mFullDecoderVersion;
    }

    public String getName() {
        return this.mName;
    }

    public String getScannerFirmwareVersion() {
        return this.mScanEngineFirmwareVersion;
    }

    public String getScannerId() {
        return this.mScanEngineId;
    }

    public String getScannerSerialNumber() {
        return this.mScanEngineSerialNumber;
    }

    public String getScannerVersionNumber() {
        return this.mScanEngineVersionNumber;
    }

    BarcodeReaderInfo(String str, String str2, String str3, String str4, String str5, String str6, String str7, int i, int i2) {
        this.mName = str;
        this.mFriendlyName = str2;
        this.mScanEngineId = str3;
        this.mFullDecoderVersion = str4;
        this.mFastDecoderVersion = str5;
        this.mControlLogicVersion = str6;
        this.mScanEngineVersionNumber = str7;
        this.mFrameHeight = i;
        this.mFrameWidth = i2;
    }
}
