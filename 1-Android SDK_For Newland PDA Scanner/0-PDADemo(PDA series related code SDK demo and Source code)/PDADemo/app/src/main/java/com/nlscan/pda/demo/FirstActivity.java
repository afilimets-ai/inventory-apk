package com.nlscan.pda.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.nlscan.pda.demo.code_type.AimidDemoActivity;
import com.nlscan.pda.demo.scan.ScanDemoActivity;
import com.nlscan.pda.demo.symbology.SymbologyActivity;

/**
 * @author Alan
 * @Company nlscan
 * @date 2017/12/16 23:02
 * @Description:
 */
public class FirstActivity extends AppCompatActivity implements View.OnClickListener{
    Button btnScanDemo;
    Button btnAimidDemo;
    Button btnSymbologyDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        btnScanDemo = (Button)findViewById(R.id.btnScanDemo);
        btnScanDemo.setOnClickListener(this);
        btnAimidDemo = (Button)findViewById(R.id.btnAimidDemo);
        btnAimidDemo.setOnClickListener(this);
        btnSymbologyDemo = (Button)findViewById(R.id.btnSymbologyDemo);
        btnSymbologyDemo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnScanDemo:
                Intent openScanDemo = new Intent(FirstActivity.this,ScanDemoActivity.class);
                startActivity(openScanDemo);
                break;
            case R.id.btnAimidDemo:
                Intent openAimidDemo = new Intent(FirstActivity.this,AimidDemoActivity.class);
                startActivity(openAimidDemo);
                break;
            case R.id.btnSymbologyDemo:
                Intent openSymbologyDemo = new Intent(FirstActivity.this, SymbologyActivity.class);
                startActivity(openSymbologyDemo);
                break;
        }
    }
}
