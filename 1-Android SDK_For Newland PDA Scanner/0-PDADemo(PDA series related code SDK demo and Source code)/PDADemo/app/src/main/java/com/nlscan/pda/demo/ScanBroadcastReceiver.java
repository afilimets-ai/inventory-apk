package com.nlscan.pda.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

/**
 * @author Alan
 * @Company nlscan
 * @date 2017/12/19 15:23
 * @Description:
 */
public class ScanBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ScanBroadcastReceiver";
    // Determine if ScanBroadcastReceiver is registered
    public static boolean registeredTag = false;
    EditText eText;


    public ScanBroadcastReceiver(View view) {
        super();
        eText = (EditText) view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (NLScanConstant.SCANNER_RESULT.equals(action)) {
            final String scanResult_1 = intent.getStringExtra("SCAN_BARCODE1");
            final String barcodeName = intent.getStringExtra("SCAN_BARCODE_TYPE_NAME"); // -1:unknown
            final String scanStatus = intent.getStringExtra("SCAN_STATE");
            StringBuilder sb = new StringBuilder();
            if ("ok".equals(scanStatus)) {
                if (!TextUtils.isEmpty(scanResult_1)) {
                    sb.append("Barcode:").append(scanResult_1).append("\r\n");
                }
                if (!TextUtils.isEmpty(barcodeName)) {
                    sb.append("CodeName:").append(barcodeName).append("\r\n");
                }
            } else {
                sb.append("SCAN_STATE:").append(scanStatus).append("\r\n");
            }
            eText.append(sb.toString());
            eText.setSelection(eText.getText().length());
        }
    }
}
