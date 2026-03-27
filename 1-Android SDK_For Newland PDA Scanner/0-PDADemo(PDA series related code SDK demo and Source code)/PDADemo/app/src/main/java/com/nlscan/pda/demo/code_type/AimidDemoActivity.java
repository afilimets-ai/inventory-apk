package com.nlscan.pda.demo.code_type;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import com.nlscan.pda.demo.NLScanConstant;
import com.nlscan.pda.demo.NLScanIntent;
import com.nlscan.pda.demo.R;
import com.nlscan.pda.demo.ScanBroadcastReceiver;

public class AimidDemoActivity extends AppCompatActivity{
    private final static String TAG = "AimidDemoActivityTag";

    // EditText
    EditText txtAimidOutputText;
    ScanBroadcastReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aimid_demo);

        txtAimidOutputText = (EditText)findViewById(R.id.txtAimidOutputText);
        resultReceiver = new ScanBroadcastReceiver(txtAimidOutputText);
        setApiScanMode();


    }

    // set the scan mode to API mode
    private void setApiScanMode(){
        NLScanIntent intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_MODE, 3);
        sendBroadcast(intent);
    }

    private void registerReceiver()
    {
        if(!ScanBroadcastReceiver.registeredTag){
            IntentFilter mFilter= new IntentFilter(NLScanConstant.SCANNER_RESULT);
            registerReceiver(resultReceiver, mFilter);
            ScanBroadcastReceiver.registeredTag = true;
            Log.d(TAG,"registerReceiver is called。registeredTag："+ScanBroadcastReceiver.registeredTag);
        }

    }

    private void unRegisterReceiver()
    {
        if(ScanBroadcastReceiver.registeredTag){
            try {
                unregisterReceiver(resultReceiver);
                ScanBroadcastReceiver.registeredTag = false;
                Log.d(TAG,"unRegisterReceiver is called。registeredTag："+ScanBroadcastReceiver.registeredTag);
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }
}
