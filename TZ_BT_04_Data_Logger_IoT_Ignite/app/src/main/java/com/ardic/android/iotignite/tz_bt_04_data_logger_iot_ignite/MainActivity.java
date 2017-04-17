package com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {


    private static final String TAG = "MainActivity";
    private Button mScanButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanButton=(Button) findViewById(R.id.scanButton);

        Log.i(TAG,"Create Main Activity");
    }

    public void showScanActivity(View v){
        Intent openIntent = new Intent(MainActivity.this, ScanActivity.class);
        startActivity(openIntent);
        Log.i(TAG,"Click Scan Button");
    }
}
