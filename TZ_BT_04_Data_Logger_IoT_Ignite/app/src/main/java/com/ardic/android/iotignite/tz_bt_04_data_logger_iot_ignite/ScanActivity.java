package com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.TZONE.Bluetooth.BLE;
import com.TZONE.Bluetooth.ILocalBluetoothCallBack;
import com.TZONE.Bluetooth.Temperature.BroadcastService;
import com.TZONE.Bluetooth.Temperature.Model.Device;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 4/17/17.
 */

//*****************************************************************************************************************
//***************** TODO: Scan Activity Class
//*****************************************************************************************************************
public class ScanActivity extends Activity {

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TODO: DEFINITIONS
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private static final String TAG = "ScanActivity";
    private TextView mScanTextView;
    private TextView mScanDataTextView;


    private BroadcastService mBroadcastService;
    private BluetoothAdapter mBluetoothAdapter;

    private boolean _IsInit = false;

    private Timer mTimer;
    private long mTimerDelay=1000;
    private long mTimerPeriod=500;

    private boolean mTZoneSdkFlag;

    private List<Device> mDeviceList = new ArrayList<>();

    private String mDeviceTemperature="";
    private String mDeviceHumidity="";
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //++++++++++++++++++++++++++++++++++++++ DEFINITIONS END ++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++





    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TODO: Activity Override
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //-------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------TODO: On Create --------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mScanTextView=(TextView) findViewById(R.id.scanTextView);
        mScanDataTextView=(TextView)findViewById(R.id.scandataTextView);

        Log.i(TAG,"Create Scan Activity");
        define_TZ_BT04();
    }
    //--------------------------------------------------On Create End ---------------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------TODO: On Resume -------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"Entered On Resume");
        try {
            if (mTimer!=null){
                mTimer.cancel();
                Log.i(TAG,"Control Timer and Cancel");
            }
            mTimer=new Timer();
            Log.i(TAG,"Create nem Timer");

            TimerTask mTimerTask= new TimerTask() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            if (_IsInit) {
                                mBroadcastService.StartScan();
                                Log.i(TAG, "Scan Bluetooth Devices");
                            }
                        }
                    }catch (Exception ex){
                        Log.e(TAG,"Error On Resume -> Timer Task -> Scan Bluetooth Device : "+ex.toString());
                    }
                }
            };

            mTimer.schedule(mTimerTask,mTimerDelay,mTimerPeriod);
        }catch (Exception ex){
            Log.e(TAG,"Error On Resume : "+ex.toString());
        }
    }
    //--------------------------------------------------On Resume End ---------------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: On Destroy -------------------------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"Entered On Destroy");

        try{
            mTimer.cancel();
            if (mBroadcastService!=null){
                mBroadcastService.StopScan();
            }
            Log.i(TAG,"Timer and Scan Stop...");
        }catch (Exception ex){
            Log.e(TAG,"Error On Destroy : "+ex.toString());
        }
    }
    //--------------------------------------------------On Destroy End ---------------------------------------------
    //-------------------------------------------------------------------------------------------------------------

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++ Activity Override End +++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++





    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TODO: TZONE SDK
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: ILocalBluetoothCallBack ------------------------------
    public ILocalBluetoothCallBack mLocalBluetoothCallBack=new ILocalBluetoothCallBack() {
        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnEntered(BLE ble) {
            Log.i(TAG,"Entered TZONE SDK -> ILocalBluetoothCallBack -> On Entered");
            try {
                AddOrUpdate(ble);
                Log.i(TAG,"Entered TZONE SDK -> ILocalBluetoothCallBack -> On Entered > Run Add or Update Function");
            }catch (Exception ex){
                Log.e(TAG,"Error !!! Entered TZONE SDK -> ILocalBluetoothCallBack -> On Entered :"+ ex.toString());
            }
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnUpdate(BLE ble) {
            Log.i(TAG,"Entered TZONE SDK -> ILocalBluetoothCallBack -> On Update");
            try {
                AddOrUpdate(ble);
                Log.i(TAG,"Entered TZONE SDK -> ILocalBluetoothCallBack -> On Update > Run Add or Update Function");
            }catch (Exception ex){
                Log.e(TAG,"Error !!! Entered TZONE SDK -> ILocalBluetoothCallBack -> On Update :"+ ex.toString());
            }
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnExited(BLE ble) {
            Log.i(TAG,"Entered TZONE SDK -> ILocalBluetoothCallBack -> On Exited");
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnScanComplete() {
            Log.i(TAG,"Entered TZONE SDK -> ILocalBluetoothCallBack -> On Scan Complate");
        }
        //-------------------------------------------------------------------------------------------------------------
    };
    //---------------------------------------------ILocalBluetoothCallBack End ---------------------------------------------
    //-------------------------------------------------------------------------------------------------------------

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TZONE SDK END +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++





    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TODO: Functions
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: Define TZ-BT04 ---------------------------------------
    private void define_TZ_BT04(){
        Log.i(TAG,"Entered Define TZ-BT04 Func");

        try{
            if (mBroadcastService==null){
                mBroadcastService=new BroadcastService();
                Log.i(TAG,"Control Broadcast Service");
                Log.i(TAG,"Create Broadcast Service");
            }

            final BluetoothManager mBluetoothManager= (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            Log.i(TAG,"Create Bluetooth Manager");

            mBluetoothAdapter=mBluetoothManager.getAdapter();
            Log.i(TAG,"Create Bluetooth Adapter");

            if (mBroadcastService.Init(mBluetoothAdapter,mLocalBluetoothCallBack)){
                _IsInit=true;
                Log.i(TAG,"Bluetooth Found !");
            }else {
                _IsInit=false;
                Toast.makeText(ScanActivity.this,"Bluetooth not found...",Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Bluetooth Not Found !!!");
                return;
            }
        }catch (Exception ex){
            Toast.makeText(ScanActivity.this,"Error : "+ex.toString(),Toast.LENGTH_SHORT).show();
        }
        mScanDataTextView.setText("Scanning...");
    }
    //--------------------------------------------------On Define TZ-BT04 End -------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: Add Or Update ---------------------------------------
    Date LastUpdateTime=new Date();
    private void AddOrUpdate(final BLE ble){
        Log.i(TAG,"Entered Add or Update Function. ");

        try {
            final Device mDevice=new Device();
            Log.i(TAG,"Create Device (TZONE SDK)");

            mDevice.fromScanData(ble);
            Log.i(TAG,"Device Set BLE Data : "+ble);

            if (mDevice==null || mDevice.SN==null || mDevice.SN.length()!=8){
                Log.i(TAG,"Data Control ..."
                        + "\nDevice : " + mDevice
                        + "\nDevice Serial Number : "+mDevice.SN);
                return;
            }

            mTZoneSdkFlag=true;
            for (int i=0;i<mDeviceList.size();i++){
                Device item=mDeviceList.get(i);
                if (item.SN.equals((mDevice.SN))){
                    mTZoneSdkFlag=false;
                }
            }

            if(mTZoneSdkFlag){
                mDeviceList.add(mDevice);
            }



            Date now = new Date();
            Long TotalTime=(now.getTime()-LastUpdateTime.getTime());
            if (TotalTime>3000){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this){
                            String mDeviceString="";
                            for (int i=0;i<mDeviceList.size();i++) {

                                if (mDeviceList.get(i).Temperature == 1000) {
                                    mDeviceTemperature="--";
                                }else {
                                    mDeviceTemperature=String.valueOf(mDeviceList.get(i).Temperature);
                                }

                                if (mDeviceList.get(i).Humidity == -1000){
                                    mDeviceHumidity="--";
                                }else{
                                    mDeviceHumidity=String.valueOf(mDeviceList.get(i).Humidity);
                                }

                                mDeviceString+=(i+1)
                                        + ". Device SN : " + mDeviceList.get(i).SN
                                        + "\nTemperature : " + mDeviceList.get(i).Temperature + "℃"
                                        + "\nHumidity : " + mDeviceList.get(i).Humidity + "%"
                                        + "\nBattery : " + mDeviceList.get(i).Battery + "%";

                                Log.i(TAG,"Device Information : " + mDeviceString+"\n\n");
                            }
                            mScanDataTextView.setText(mDeviceString);
                        }
                    }
                });
                LastUpdateTime=now;
            }

        }catch (Exception ex){
            Log.e(TAG,"Error Add or Update Function : "+ex.toString());
        }
    }
    //-------------------------------------------------- Add Or Update End ----------------------------------------
    //-------------------------------------------------------------------------------------------------------------
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ Functions End +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}
//*****************************************************************************************************************
//************************************** Scan Activity Class End ***************************************************
//*****************************************************************************************************************
