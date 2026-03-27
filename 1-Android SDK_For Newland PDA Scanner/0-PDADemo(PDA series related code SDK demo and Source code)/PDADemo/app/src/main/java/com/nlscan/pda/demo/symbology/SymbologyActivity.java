package com.nlscan.pda.demo.symbology;
/**
 * @Company nlscan
 * @author Alan
 * @date 2022/11/06
 * @Description:
 */

import static com.nlscan.pda.demo.NLScanConstant.CODE_ID;
import static com.nlscan.pda.demo.NLScanConstant.PROPERTY;
import static com.nlscan.pda.demo.NLScanConstant.VALUE;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nlscan.pda.demo.NLScanConstant;
import com.nlscan.pda.demo.NLScanIntent;
import com.nlscan.pda.demo.R;

public class SymbologyActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "SymbologyActivity";
    //EnableQR RadioButton
    RadioGroup radEnableQRGroup;
    RadioButton radQROn;
    RadioButton radQROff;

    EditText txtQRMaxLen;
    EditText txtQRMinLen;

    Button btnOK;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symbology_setting);

        //Enable QR Group RadioButton
        radEnableQRGroup = (RadioGroup) findViewById(R.id.radEnableQRGroup);
        radEnableQRGroup.setOnCheckedChangeListener(new SymbologyActivity.RadioGroupListener());
        radQROn = (RadioButton) findViewById(R.id.radQROn);
        radQROff = (RadioButton) findViewById(R.id.radQROff);

        //Text
        txtQRMaxLen = (EditText)findViewById(R.id.txtQRMaxLen);
        txtQRMinLen = (EditText)findViewById(R.id.txtQRMinLen);

        //Button
        btnOK = (Button) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOK:
                btnOKClicked();
                break;
            case R.id.btnCancel:
                btnCancelClicked();
                break;
            default:
                break;
        }
    }

    private void btnCancelClicked() {
        radEnableQRGroup.clearCheck();
        txtQRMaxLen.setText("");
        txtQRMinLen.setText("");

    }

    private void btnOKClicked() {
        String strMax=txtQRMaxLen.getText().toString();
        String strMin=txtQRMinLen.getText().toString();
        if(checkLength(strMax,strMin)){
            if(!strMax.isEmpty()){
                NLScanIntent intentMax = new NLScanIntent(NLScanConstant.ACTION_BARCODE_CFG);
                intentMax.putExtra(CODE_ID,"QR");
                intentMax.putExtra(PROPERTY,"Maxlen");
                intentMax.putExtra(VALUE,strMax);
                sendBroadcast(intentMax);
            }

            if(!strMin.isEmpty()){
                NLScanIntent intentMin = new NLScanIntent(NLScanConstant.ACTION_BARCODE_CFG);
                intentMin.putExtra(CODE_ID,"QR");
                intentMin.putExtra(PROPERTY,"Minlen");
                intentMin.putExtra(VALUE,strMin);
                sendBroadcast(intentMin);
            }
            Toast.makeText(this,"Length are set successfully.",Toast.LENGTH_LONG).show();

        }
    }

    private boolean checkLength(String strMax,String strMin) {
        int intMax;
        int intMin;

        if((!strMax.isEmpty()&&strMin.isEmpty())||(strMax.isEmpty()&&!strMin.isEmpty())){
            Toast.makeText(this,"Max length and Min length should be input in same time.",Toast.LENGTH_LONG).show();
            return false;
        }

        if(!strMin.isEmpty()&&!strMax.isEmpty()){
            intMax=Integer.parseInt(strMax);
            intMin=Integer.parseInt(strMin);
            if(intMax<1||intMax>7089){
                Toast.makeText(this,"The max length of QR should between 1 and 7089.",Toast.LENGTH_LONG).show();
                return false;
            }
            if(intMin<1||intMin>7089){
                Toast.makeText(this,"The min length of QR should between 1 and 7089.",Toast.LENGTH_LONG).show();
                return false;
            }
            if(intMin>intMax){
                Toast.makeText(this,"The min length is bigger than max length.",Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            return false;
        }

        return true;
    }

    private class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        NLScanIntent intent;

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            switch (checkedId) {
                // Scan Func
                case R.id.radQROn:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BARCODE_CFG);
                    intent.putExtra(CODE_ID,"QR");
                    intent.putExtra(PROPERTY,"Enable");
                    intent.putExtra(VALUE,"1");
                    break;
                case R.id.radQROff:
                    intent = new NLScanIntent(NLScanConstant.ACTION_BARCODE_CFG);
                    intent.putExtra(CODE_ID,"QR");
                    intent.putExtra(PROPERTY,"Enable");
                    intent.putExtra(VALUE,"0");
                    break;


            }
            if(intent!=null){
                sendBroadcast(intent);
            }
        }
    }
}