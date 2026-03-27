package com.nlscan.pda.demo.scan;

/**
 * @Company nlscan
 * @author Alan
 * @date 2017/12/16 23:02
 * @Description:
 */

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nlscan.pda.demo.NLScanConstant;
import com.nlscan.pda.demo.NLScanIntent;
import com.nlscan.pda.demo.R;
import com.nlscan.pda.demo.ScanBroadcastReceiver;

public class ScanDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ScanDemoActivityTag";


    //ScanFunc RadioButton
    RadioGroup radScanFuncGroup;
    RadioButton radScanFuncOn;
    RadioButton radScanFuncOff;

    //ScanMode RadioButton
    RadioGroup radScanModeGroup;
    RadioButton radDirectOutput;
    RadioButton radAnalogOutput;
    RadioButton radBordcastOutput;

    //TrigMode RadioButton
    RadioGroup radTrigModeGroup;
    RadioButton radNormalTrig;
    RadioButton radContinueTrig;
    RadioButton radOverTimeTrig;

    //ScanAutoent RadioButton
    RadioGroup radScanAutoentGroup;
    RadioButton radScanAutoentOn;
    RadioButton radScanAutoentOff;

    //ScanSoundNotice RadioButton
    RadioGroup radScanSoundNoticeGroup;
    RadioButton radScanSoundNoticeOn;
    RadioButton radScanSoundNoticeOff;

    //ScanVibratesNotice RadioButton
    RadioGroup radScanVibratesNoticeGroup;
    RadioButton radScanVibratesNoticeOn;
    RadioButton radScanVibratesNoticeOff;

    //Scan LED Notice RadioButton
    RadioGroup radScanLEDNoticeGroup;
    RadioButton radScanLEDNoticeOn;
    RadioButton radScanLEDNoticeOff;

    // Buttons
    Button btnScan;
    Button btnClear;
    Button btnReset;

    // Scan out put
    EditText editText;
    ScanBroadcastReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_demo_activity_main);
        //Button
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.requestFocus();
        btnScan.setOnClickListener(this);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);

        //ScanFunc RadioButton
        radScanFuncGroup = (RadioGroup) findViewById(R.id.radScanFuncGroup);
        radScanFuncGroup.setOnCheckedChangeListener(new RadioGroupListener());
        radScanFuncOn = (RadioButton) findViewById(R.id.radScanFuncOn);
        radScanFuncOff = (RadioButton) findViewById(R.id.radScanFuncOff);

        //ScanMode RadioButton
        radScanModeGroup = (RadioGroup) findViewById(R.id.radScanModeGroup);
        radScanModeGroup.setOnCheckedChangeListener(new RadioGroupListener());
        radDirectOutput = (RadioButton) findViewById(R.id.radDirectOutput);
        radAnalogOutput = (RadioButton) findViewById(R.id.radAnalogOutput);
        radBordcastOutput = (RadioButton) findViewById(R.id.radBordcastOutput);

        //TrigMode RadioButton
        radTrigModeGroup = (RadioGroup) findViewById(R.id.radTrigModeGroup);
        radTrigModeGroup.setOnCheckedChangeListener(new RadioGroupListener());
        radNormalTrig = (RadioButton) findViewById(R.id.radNormalTrig);
        radContinueTrig = (RadioButton) findViewById(R.id.radContinueTrig);
        radOverTimeTrig = (RadioButton) findViewById(R.id.radOverTimeTrig);

        //ScanAutoent RadioButton
        radScanAutoentGroup = (RadioGroup) findViewById(R.id.radScanAutoentGroup);
        radScanAutoentGroup.setOnCheckedChangeListener(new RadioGroupListener());
        radScanAutoentOn = (RadioButton) findViewById(R.id.radScanAutoentOn);
        radScanAutoentOff = (RadioButton) findViewById(R.id.radScanAutoentOff);

        //ScanAutoent RadioButton
        radScanSoundNoticeGroup = (RadioGroup) findViewById(R.id.radScanSoundNoticeGroup);
        radScanSoundNoticeGroup.setOnCheckedChangeListener(new RadioGroupListener());
        radScanSoundNoticeOn = (RadioButton) findViewById(R.id.radScanSoundNoticeOn);
        radScanSoundNoticeOff = (RadioButton) findViewById(R.id.radScanSoundNoticeOff);

        //ScanVibratesNotice RadioButton
        radScanVibratesNoticeGroup = (RadioGroup) findViewById(R.id.radScanVibratesNoticeGroup);
        radScanVibratesNoticeGroup.setOnCheckedChangeListener(new RadioGroupListener());
        radScanVibratesNoticeOn = (RadioButton) findViewById(R.id.radScanVibratesNoticeOn);
        radScanVibratesNoticeOff = (RadioButton) findViewById(R.id.radScanVibratesNoticeOff);

        //Scan LED Notice RadioButton
        radScanLEDNoticeGroup = (RadioGroup) findViewById(R.id.radScanLEDNoticeGroup);
        radScanLEDNoticeGroup.setOnCheckedChangeListener(new RadioGroupListener());
        radScanLEDNoticeOn = (RadioButton) findViewById(R.id.radScanLEDNoticeOn);
        radScanLEDNoticeOff = (RadioButton) findViewById(R.id.radScanLEDNoticeOff);

        //BarCode Output EditText
        editText = (EditText) findViewById(R.id.editText);
        // Scroll the EditText
        editText.setMovementMethod(ScrollingMovementMethod.getInstance());

        resultReceiver = new ScanBroadcastReceiver(editText);
        Log.d(TAG,getClass().getSimpleName());

    }

    // When selecting the API output mode, you need to register the broadcast receiver to get the scan result
    private void registerReceiver() {
        if(!ScanBroadcastReceiver.registeredTag){
            IntentFilter mFilter = new IntentFilter(NLScanConstant.SCANNER_RESULT);
            registerReceiver(resultReceiver, mFilter);
            ScanBroadcastReceiver.registeredTag = true;
            Log.d(TAG,"registerReceiver is called.registeredTag："+ScanBroadcastReceiver.registeredTag);
        }
    }


    private void unRegisterReceiver() {
        if(ScanBroadcastReceiver.registeredTag){
            try {
                unregisterReceiver(resultReceiver);
                ScanBroadcastReceiver.registeredTag = false;
                Log.d(TAG,"unRegisterReceiver is called.registeredTag："+ScanBroadcastReceiver.registeredTag);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScan:
                btnScanClicked();
                break;
            case R.id.btnClear:
                editText.setText("");
                break;
            case R.id.btnReset:
                btnResetClicked();
                break;
            default:
                break;
        }
    }

    private void btnResetClicked() {
        radScanFuncGroup.clearCheck();
        radScanModeGroup.clearCheck();
        radTrigModeGroup.clearCheck();
        radScanAutoentGroup.clearCheck();
        radScanSoundNoticeGroup.clearCheck();
        radScanVibratesNoticeGroup.clearCheck();
        radScanLEDNoticeGroup.clearCheck();
    }
    private void btnScanClicked() {

        editText.requestFocus();
        NLScanIntent intent = new NLScanIntent(NLScanConstant.SCANNER_TRIG);
        sendBroadcast(intent);
        editText.setSelection(editText.getText().length());

    }

    private class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        NLScanIntent intent;

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            switch (checkedId) {
                // Scan Func
                case R.id.radScanFuncOn:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_POWER, 1);
                    Log.d(TAG, "You choosed Open Scan Power");
                    break;
                case R.id.radScanFuncOff:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_POWER, 0);
                    Log.d(TAG, "You choosed Close Scan Power");
                    break;
                // Scan Mode
                case R.id.radDirectOutput:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_MODE, 1);
                    Log.d(TAG, "You choosed DirectOutput Mode");
                    break;
                case R.id.radAnalogOutput:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_MODE, 2);
                    Log.d(TAG, "You choosed AnalogOutput Mode");
                    break;
                case R.id.radBordcastOutput:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_MODE, 3);
                    Log.d(TAG, "You choosed BordcastOutput Mode");
                    break;
                // Trig Mode
                case R.id.radNormalTrig:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_TRIG_MODE, 0);
                    Log.d(TAG, "You choosed Normal Trig Mode");
                    break;
                case R.id.radContinueTrig:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_TRIG_MODE, 1);
                    Log.d(TAG, "You choosed Continue Trig Mode");
                    break;
                case R.id.radOverTimeTrig:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_TRIG_MODE, 2);
                    Log.d(TAG, "You choosed OverTime Trig Mode");
                    break;
                // ScanAutoent
                case R.id.radScanAutoentOn:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_AUTOENT, 1);
                    Log.d(TAG, "You choosed Scan Autoent On");
                    break;
                case R.id.radScanAutoentOff:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_AUTOENT, 0);
                    Log.d(TAG, "You choosed Scan Autoent Off");
                    break;
                // ScanSoundNotice
                case R.id.radScanSoundNoticeOff:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_NOTY_SND, 0);
                    Log.d(TAG, "You choosed Scan Sound Notice Off");
                    break;
                case R.id.radScanSoundNoticeOn:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_NOTY_SND, 1);
                    Log.d(TAG, "You choosed Scan Sound Notice On");
                    break;
                // ScanSoundNotice
                case R.id.radScanVibratesNoticeOff:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_NOTY_VIB, 0);
                    Log.d(TAG, "You choosed Scan Sound Notice Off");
                    break;
                case R.id.radScanVibratesNoticeOn:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_NOTY_VIB, 1);
                    Log.d(TAG, "You choosed Scan Sound Notice On");
                    break;
                // ScanSoundNotice
                case R.id.radScanLEDNoticeOff:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_NOTY_LED, 0);
                    Log.d(TAG, "You choosed Scan Sound Notice Off");
                    break;
                case R.id.radScanLEDNoticeOn:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BAR_SCANCFG, NLScanConstant.EXTRA_SCAN_NOTY_LED, 1);
                    Log.d(TAG, "You choosed Scan Sound Notice On");
                    break;

            }
            if(intent!=null){
                sendBroadcast(intent);
            }
        }
    }

}
