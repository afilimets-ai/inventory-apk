package com.honeywell.aidc;

/* JADX INFO: loaded from: classes.dex */
public final class ScannerNotClaimedException extends AidcException {
    private static final long serialVersionUID = 1;

    ScannerNotClaimedException(String str) {
        super(str);
    }

    ScannerNotClaimedException(String str, Throwable th) {
        super(str, th);
    }
}
