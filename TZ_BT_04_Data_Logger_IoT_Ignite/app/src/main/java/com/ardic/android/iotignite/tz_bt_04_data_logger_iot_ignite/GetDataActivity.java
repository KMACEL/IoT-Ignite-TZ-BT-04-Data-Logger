package com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite;

import android.app.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

import android.os.Bundle;
import android.util.Log;

import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.TZONE.Bluetooth.BLE;
import com.TZONE.Bluetooth.BLEGattService;
import com.TZONE.Bluetooth.IConfigCallBack;
import com.TZONE.Bluetooth.ILocalBluetoothCallBack;
import com.TZONE.Bluetooth.Temperature.BroadcastService;
import com.TZONE.Bluetooth.Temperature.ConfigService;
import com.TZONE.Bluetooth.Temperature.Model.CharacteristicHandle;
import com.TZONE.Bluetooth.Temperature.Model.CharacteristicType;
import com.TZONE.Bluetooth.Temperature.Model.Device;
import com.TZONE.Bluetooth.Utils.BinaryUtil;
import com.TZONE.Bluetooth.Utils.DateUtil;
import com.TZONE.Bluetooth.Utils.StringConvertUtil;
import com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite.Model.Report;
import com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite.Model.ReportData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by root on 4/17/17.
 */
//*****************************************************************************************************************
//***************** TODO: Get Data Activity Class
//*****************************************************************************************************************
public class GetDataActivity extends Activity{

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TODO: DEFINITIONS
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private static final String TAG = "GetDataActivity";
    private TextView mGetDataTextView;
    private TextView txtPrint;

    private String SN = "";
    private String Password = "000000";
    private String DeviceName = "";
    private String HardwareModel = "3A01";
    private String Firmware = "";
    private Report mReport;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastService mBroadcastService;
    private ConfigService mConfigService;
    public Queue<byte[]> Buffer = new LinkedList<byte[]>();
    private int _SyncCount = 0;
    private int _SyncIndex = 0;
    private int _SyncProgress=0;

    private int getDataSize=25;
    private long configServiceTimeOut=30000;

    private  String mHumidity;
    private String Temperature;
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
        setContentView(R.layout.activity_get_data);
        txtPrint=(TextView)findViewById(R.id.getDataTextView);
    }
    //--------------------------------------------------On Create End ---------------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: On Destroy -------------------------------------------
    @Override
    protected void onDestroy() {
        Log.i(TAG,"Entered On Destroy");
        try {
            if(mBroadcastService!=null)
                mBroadcastService.StopScan();
            if(mConfigService!=null)
                mConfigService.Dispose();
        }catch (Exception ex){
            Log.e(TAG,"On Destroy Error : " + ex.toString());
        }
        super.onDestroy();
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
    public ILocalBluetoothCallBack mLocalBluetoothCallBack = new ILocalBluetoothCallBack() {
        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnEntered(BLE ble) {
            Log.i(TAG,"Entered LocalBluetoothCallBack -> On Entered ");
            final Device device = new Device();
            device.fromScanData(ble);
            Log.i(TAG,"Creade Device... LocalBluetoothCallBack -> On Entered ");

            if(device.SN!=null && device.SN.equals(SN)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connect(device);
                        Log.i(TAG,"Connect Function : LocalBluetoothCallBack -> On Entered ");
                    }
                });
            }
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnUpdate(BLE ble) {

        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnExited(BLE ble) {

        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnScanComplete() {

        }
        //-------------------------------------------------------------------------------------------------------------

    };
    //---------------------------------------------ILocalBluetoothCallBack End --------------------------------------
    //---------------------------------------------------------------------------------------------------------------



    //----------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: IConfigCallBack -----------------------------------------
    public IConfigCallBack mIConfigCallBack = new IConfigCallBack() {
        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnReadConfigCallBack(boolean status, final HashMap<String, byte[]> uuids) {
            if(status) {
                Log.i(TAG,"Entered IConfig Call Back");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Device device = mConfigService.GetCofing(uuids);
                        syncData(device);
                    }
                });
            }
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnWriteConfigCallBack(boolean status) {

        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnConnected() {

        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnDisConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GetDataActivity.this, "Disconnected!", Toast.LENGTH_SHORT).show();
                    finish();

                }
            });
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnServicesed(List<BLEGattService> services) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConfigService.CheckToken(Password);
                }
            });
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnReadCallBack(UUID uuid, byte[] data) {

        }
        //-------------------------------------------------------------------------------------------------------------


        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnWriteCallBack(UUID uuid,final boolean isSuccess) {
            if (uuid.toString().toLowerCase().equals(new CharacteristicHandle().GetCharacteristicUUID(CharacteristicType.Token).toString().toLowerCase())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ReadConfig();
                    }
                });
            }
        }
        //-------------------------------------------------------------------------------------------------------------



        //-------------------------------------------------------------------------------------------------------------
        @Override
        public void OnReceiveCallBack(UUID uuid, byte[] data) {
            try {
                int serial = Integer.parseInt(StringConvertUtil.bytesToHexString(BinaryUtil.CloneRange(data, data.length - 3, 2)),16); //data[data.length - 3] * 256 + data[data.length - 2];
                String crc = StringConvertUtil.bytesToHexString(BinaryUtil.CloneRange(data, data.length - 1, 1));
                String checksum = CRC(BinaryUtil.CloneRange(data, 0, data.length - 1));

                /*if (!checksum.equals(crc)) {
                    Log.e("SyncActivity", "SN：" + serial + " crc:" + crc + " error");
                    return;
                }*/

                if((HardwareModel.equals("3901") && Integer.parseInt(Firmware) < 20)
                        ||(HardwareModel.equals("3A01") && Integer.parseInt(Firmware) < 7)) {
                    if (data.length >= 9) {
                        Buffer.add(BinaryUtil.CloneRange(data, 0, 6));
                        _SyncIndex++;
                    }
                    if (data.length >= 15) {
                        Buffer.add(BinaryUtil.CloneRange(data, 6, 6));
                        _SyncIndex++;
                    }
                }else {
                    if (data.length >= 10) {
                        Buffer.add(BinaryUtil.CloneRange(data, 0, 7));
                        _SyncIndex++;
                    }
                    if (data.length >= 17) {
                        Buffer.add(BinaryUtil.CloneRange(data, 7, 7));
                        _SyncIndex++;
                    }
                }

                final int syncProgress = (int)((Double.parseDouble(_SyncIndex+"") / _SyncCount) * 100);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isComplete = false;
                     //   int progress = syncProgress;
                        if(_SyncIndex >= _SyncCount){
                            _SyncIndex = _SyncCount;
                           // progress = 100;
                            isComplete = true;
                        }
                        //_SyncProgress = progress;
                        if(isComplete) {
                            showData();
                        }
                    }
                });

            }catch (Exception ex){}
        }
        //---------------------------------------------------------------------------------------------------------

    };


    //-------------------------------------------------------------------------------------------------------------
    public String CRC(byte[] data)
    {
        int num = 0;
        for (int i = 0; i < data.length; i++)
            num = (num + data[i]) % 0xffff;
        String hex = Integer.toHexString(num);
        if(hex.length()%2 > 0)
            hex = "0"+hex;
        String crc = hex.substring(hex.length()- 2);
        return  crc;
    }
    //-------------------------------------------------------------------------------------------------------------

    //--------------------------------------------- IConfigCallBack End -------------------------------------------
    //-------------------------------------------------------------------------------------------------------------

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TZONE SDK END +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TODO: Button Click
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public void getDataButtonClick(View v){
        Log.i(TAG,"Click Get Data Button");
        try {
            txtPrint.setText("Scanning...");
            SN = "11162240";
            Password = "000000";
            Log.i(TAG,"Device Id : " + SN + "\nDevice Password : "+"******");

            mReport = new Report();
            mReport.SN = SN;

            Log.i(TAG,"Report create and send Device Id");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    scan();
                    Log.i(TAG,"Get Data Button Runnable scan...");
                }
            });

        } catch (Exception ex) {
            Log.e(TAG,"Error Get Data Button Click : "+ex.toString());
        }

    }
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ Button Click END ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++





    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ TODO: Functions
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: Scan -------------------------------------------------
    protected void scan(){
        try {

            if(mBroadcastService == null){
                mBroadcastService = new BroadcastService();
                Log.i(TAG,"Control and Create Broadcast Service...");
            }

            final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            Log.i(TAG,"Create Bluetooth Manager and Bluetooth Adapter");

            if(mBroadcastService.Init(mBluetoothAdapter,mLocalBluetoothCallBack)){
                Log.i(TAG,"Control Bluetooth Service, Callback");

            }else {
                Toast.makeText(GetDataActivity.this, "Bluetooth not found !", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Bluetooth not found !");
            }

            mBroadcastService.StartScan();
            Log.i(TAG,"Broadcast Service Start");

        }catch (Exception ex){

            Toast.makeText(GetDataActivity.this, "Could not find the device!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Could not find the device! "+ex.toString());
            finish();
        }
    }
    //--------------------------------------------------Scan End --------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: Connect ----------------------------------------------
    protected void connect(Device device){
        Log.i(TAG,"Entered connect Function");
        try {
            mBroadcastService.StopScan();

            Log.i(TAG,"Stop Broadcast Service");

            if(device != null) {

                if(device.Name != null && !device.Name.equals("")) {
                    DeviceName = device.Name;
                }
                HardwareModel = device.HardwareModel;
                Firmware = device.Firmware;

                if(device.Battery != 1000) {
                    mReport.Battery = device.Battery;
                }

                if(device.HardwareModel.equals("3901")) {
                    mReport.ProductType = "BT04";
                } else  if(device.HardwareModel.equals("3A01")) {
                    mReport.ProductType = "BT05";
                } else {
                    mReport.ProductType = "BT(" + device.HardwareModel + ")";
                }
                mReport.FirmwareVersion = device.Firmware;

                Log.i(TAG, "Device Control... " + device);
                Log.i(TAG, "Device Name : "+device.Name
                         + "\nDevice Hardware Model : "+device.HardwareModel
                         + "\nDevice Firmware : " + device.Firmware
                         + "\nDevice Battery : " + device.Battery
                         + "\nDevice Product Type : " + mReport.ProductType
                         + "\nDevice Mac Adress : " + device.MacAddress);
            }



            if(mConfigService != null){
                mConfigService.Dispose();//*/****************
            }

            mConfigService = new ConfigService(mBluetoothAdapter,this,device.MacAddress,configServiceTimeOut, mIConfigCallBack);//***************
            Log.i(TAG,"Create Config Service");

        }catch (Exception ex){
            Toast.makeText(GetDataActivity.this, "Unable to connect the device!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Unable to connect the device! : " + ex.toString());
            finish();
        }
    }
    //-------------------------------------------------- Connect End ----------------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: Read Config ------------------------------------------
    protected void ReadConfig(){
        Log.i(TAG,"Entered Read Config Function");
        try {

            if(HardwareModel.equals("3901")) {
                mConfigService.ReadConfig_BT04(Firmware);
            }else {
                mConfigService.ReadConfig_BT05(Firmware);
            }
            Log.i(TAG,"Hardware Model Control  ");

        }catch (Exception ex){
            Toast.makeText(GetDataActivity.this, "Can not read the device parameters! :" +ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    //-------------------------------------------------- Read Config End -----------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: SYNC Data --------------------------------------------
    protected void syncData(Device device){
        Log.i(TAG,"Entered SYNC Data Function...");
        try {


            if(device != null) {
                Log.i(TAG,"Sync Data Device Control");

                if(device.Name != null && !device.Name.equals("")) {
                    DeviceName = device.Name;
                }

                if(device.SavaCount != 1000) {
                    _SyncCount = device.SavaCount;
                }

                mReport.Name = DeviceName + "("+device.SN+") -- "+ DateUtil.ToString(DateUtil.GetUTCTime(),"yyyyMMdd");
                if(device.Notes != null) {
                    mReport.Notes = device.Notes;
                }

                if(device.Description != null) {
                    mReport.Description = device.Description;
                }

                mReport.SamplingInterval = device.SaveInterval; //device.SamplingInterval;
                mReport.LT = device.LT;
                mReport.HT = device.HT;
            }

            if(device.SavaCount == -1000 || device.SavaCount == 0){
                Toast.makeText(GetDataActivity.this, "Data record is empty! Without extracting", Toast.LENGTH_SHORT).show();
                finish();
            }

            mConfigService.Sync(true);

            Log.i(TAG,"SYNC Data Function :"
                    + "\nDevice Name :" + DeviceName
                    + "\nSync Count : " + _SyncCount
                    + "\nReport Notes : " + mReport.Notes
                    + "\nReport Description : " + mReport.Description
                    + "\nReport Sampling Interval : " +  mReport.SamplingInterval
                    + "\nReport LT : " + mReport.LT
                    + "\nReport HT : " + mReport.HT);

        }catch (Exception ex){
            Toast.makeText(GetDataActivity.this, "Extract data during abnormal! : " + ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Extract data during abnormal");
            finish();
        }
    }
    //-------------------------------------------------- SYNC Data End --------------------------------------------
    //-------------------------------------------------------------------------------------------------------------



    //-------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ TODO: Show Data --------------------------------------------
    protected void showData(){
        Log.i(TAG,"Entered Show Data Function");
        try {
            mConfigService.Dispose();
            Log.i(TAG,"Config Service Dispose...");

           // _ProgressDialog = new ProgressDialog(this).show(this,"","please wait...",true,false,null);
            // _ProgressDialog.dismiss();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        double totalTemperature = 0;
                        int totalTemperature_count = 0;
                        double totalHumidity = 0;
                        int totalHumidity_count = 0;
                        Log.i(TAG,"Reset Total Variable ...");

                        while (Buffer.size() > 0) {
                           // Log.i(TAG,"Buffer Size Loop ...");

                            Device device = new Device();
                           // Log.i(TAG,"Create Device ...");

                            device.HardwareModel = HardwareModel;
                           // Log.i(TAG,"Show Data : Device Hardware Model : " + device.HardwareModel);

                            device.Firmware = Firmware;
                            //Log.i(TAG,"Show Data : Device Firmware : " + device.Firmware );

                            device.fromNotificationData(Buffer.remove());
                           // Log.i(TAG,"Get Device From Notification Data : Set Buffer");

                            if(device !=null && device.UTCTime != null){
                              //  Log.i(TAG,"Device Data Control...");

                                if(device.Temperature != -1000) {
                                    totalTemperature += device.Temperature;
                                    totalTemperature_count ++;
                                  //  Log.i(TAG,totalTemperature_count+ "Temperature : " + device.Temperature);
                                }

                                if(device.Humidity != -1000) {
                                    totalHumidity += device.Humidity;
                                    totalHumidity_count ++;
                                   // Log.i(TAG,totalHumidity_count+ "Temperature : " + device.Humidity);
                                }

                                ReportData reportData = new ReportData(device);
                               // Log.i(TAG,"Report Data Create and Send Device");
                                mReport.Data.add(reportData);
                            }
                        }


                        mReport.DataCount = mReport.Data.size();
                        if(mReport.Data.size() > 0 ) {
                            mReport.BeginTime = mReport.Data.get(0).RecordTime;
                            mReport.EndTime = mReport.Data.get(mReport.Data.size() - 1).RecordTime;

                            Log.i(TAG,"Data Get Begin Time : " + mReport.BeginTime);
                            Log.i(TAG,"Data Get End Time : " + mReport.EndTime );
                        }

                        mReport.Generate();
                        Log.i(TAG,"Report Generate ....");

                        GetDataActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String printText = "";

                                Log.i(TAG,"Report Data Size : " +mReport.Data.size());
                                Log.i(TAG,"Get Data Size : " + getDataSize);

                                for (int i = 0; i < mReport.Data.size() && i < getDataSize; i++) {
                                    ReportData item = mReport.Data.get(i);
                                    Log.i(TAG,"Item Add : " + mReport.Data.get(i));

                                    printText += (i + 1) + ".";
                                    double tzone = DateUtil.GetTimeZone() / 60.0;

                                    printText += DateUtil.ToString(DateUtil.DateAddHours(item.RecordTime,tzone), "yyyy-MM-dd HH:mm:ss")+" ";
                                    if(item.Temperature != -1000) {
                                        printText += "Temperature:" + item.Temperature + "℃";
                                        Temperature = String.valueOf(item.Temperature);
                                    }else {
                                        printText += "Temperature:--℃";
                                        Temperature="--";
                                    }
                                    if(item.Humidity != -1000) {
                                        printText += "Humidity:" + item.Humidity + "%";
                                        mHumidity = String.valueOf(item.Humidity);
                                    }else {
                                        printText += "Humidity:--%";
                                        mHumidity="--";
                                    }
                                    printText += "\n\n";

                                    Log.i(TAG,"Get Time : " + (DateUtil.GetTimeZone() / 60.0)
                                            + "\nTemperature : " + item.Temperature+ "℃"
                                            + "\nHumidity:" + item.Humidity+"%");
                                    Log.i(TAG, "\n-----------------\n");

                                }
                                txtPrint.setText(printText + "......");
                            }
                        });

                    }catch (Exception ex){
                        Log.e(TAG,"Show Data Error ... : " + ex.toString());
                    }
                }
            }).start();

        }catch (Exception ex){
            Toast.makeText(GetDataActivity.this, " Show Data Error :"+ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Show Data Error ... : " + ex.toString());
            finish();
        }
    }
    //-------------------------------------------------- Show Data End --------------------------------------------
    //-------------------------------------------------------------------------------------------------------------

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++ Functions End +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}

//*****************************************************************************************************************
//************************************** Get Data Activity Class End **********************************************
//*****************************************************************************************************************

