package com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class GetDataActivity extends Activity{


    private static final String TAG = "GetDataActivity";
    private TextView mGetDataTextView;
    private TextView txtPrint;

    private ProgressDialog _ProgressDialog;
    private Dialog _AlertDialog;

    private String SN = "";
    private String Password = "000000";
    private String DeviceName = "";
    private String HardwareModel = "3A01";
    private String Firmware = "";
    private Report mReport;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastService mBroadcastService;
    private ConfigService _ConfigService;
    private boolean _IsInit = false;
    private boolean _IsScanning = false;
    private boolean _IsConnecting = false;
    private boolean _IsReading = false;
    private boolean _IsSync = false;
    public Queue<byte[]> Buffer = new LinkedList<byte[]>();
    private int _SyncCount = 0;
    private int _SyncIndex = 0;
    private int _SyncProgress = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_data);
        txtPrint=(TextView)findViewById(R.id.getDataTextView);


    }

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
                    Scan();
                    Log.i(TAG,"Get Data Button Runnable Scan...");
                }
            });

        } catch (Exception ex) {
            Log.e(TAG,"Error Get Data Button Click : "+ex.toString());
        }

    }

    protected void Scan(){
        try {

            if(mBroadcastService == null){
                mBroadcastService = new BroadcastService();
                Log.i(TAG,"Control and Create Broadcast Service...");
            }

            final BluetoothManager bluetoothManager =(BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            Log.i(TAG,"Create Bluetooth Manager and Bluetooth Adapter");

            if(mBroadcastService.Init(mBluetoothAdapter,_LocalBluetoothCallBack)){
                _IsInit = true;
                Log.i(TAG,"Control Bluetooth Service, Callback");
            }else {
                _IsInit = false;
                Toast.makeText(GetDataActivity.this, "Bluetooth not found !", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"Bluetooth not found !");
                return;
            }

            if(_IsScanning) {
                return;
            }


            _IsScanning = true;
            mBroadcastService.StartScan();
            Log.i(TAG,"Broadcast Service Start");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"Entered Scan Runnable");
                    try {
                        Thread.sleep(15000);
                        if(_IsScanning){
                            mBroadcastService.StopScan();
                            _IsScanning = false;
                            Log.i(TAG,"Scan Complete...");
                        }
                    }catch (Exception ex){
                        Log.e(TAG,"Scan Runnable Error : "+ex.toString());
                    }
                }
            }).start();


        }catch (Exception ex){
            _IsInit = false;
            _IsScanning = false;
            Toast.makeText(GetDataActivity.this, "Could not find the device!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Could not find the device! "+ex.toString());
            finish();
        }
    }

    /**
     * Connect
     */
    protected void Connect(Device device){
        Log.i(TAG,"Entered Connect Function");
        try {
            if(_IsConnecting) {
                Log.i(TAG,"Connection Control");
                return;
            }
            mBroadcastService.StopScan();
            _IsScanning = false;
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

            _IsConnecting = true;

            if(_ConfigService != null){
                _ConfigService.Dispose();//*/****************
            }

            _ConfigService = new ConfigService(mBluetoothAdapter,this,device.MacAddress,30000,_IConfigCallBack);
            Log.i(TAG,"Create Config Service");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(30000);
                        if(_IsConnecting) {
                            _ProgressDialog.cancel();
                            _IsConnecting = false;
                            Log.i(TAG,"Start Connect Runnable");
                        }
                    }catch (Exception ex){
                        Log.e(TAG,"Connect Runnable Error : " + ex.toString());
                    }
                }
            }).start();
        }catch (Exception ex){
            _IsConnecting = false;
            Toast.makeText(GetDataActivity.this, "Unable to connect the device!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Unable to connect the device! : " + ex.toString());
            finish();
        }
    }

    /**
     * Read Config
     */
    protected void ReadConfig(){
        Log.i(TAG,"Entered Read Config Function");
        try {
            if(_IsReading)
                return;

            _IsConnecting = false;


            _IsReading = true;
            if(HardwareModel.equals("3901"))
                _ConfigService.ReadConfig_BT04(Firmware);
            else
                _ConfigService.ReadConfig_BT05(Firmware);

            if(_ProgressDialog!=null && _ProgressDialog.isShowing())
                _ProgressDialog.dismiss();
            _ProgressDialog = new ProgressDialog(this).show(this,"","Reading device parameters...",true,true,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(GetDataActivity.this, "Can not read the device parameters!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(15000);
                        if(_IsReading){
                            _ProgressDialog.cancel();
                            _IsReading = false;
                        }
                    }catch (Exception ex){}
                }
            }).start();

        }catch (Exception ex){
            _IsReading = false;
            Toast.makeText(GetDataActivity.this, "Can not read the device parameters!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Sync Data
     */
    protected void SyncData(Device device){
        try {
            if(_IsSync)
                return;

            _IsReading = false;

            _SyncIndex = 0;
            _SyncCount = 0;
            _SyncProgress = 0;
            Buffer.clear();

            if(device != null) {
                if(device.Name != null && !device.Name.equals(""))
                    DeviceName = device.Name;
                if(device.SavaCount != 1000) {
                    _SyncCount = device.SavaCount;
                }

                mReport.Name = DeviceName + "("+device.SN+") -- "+ DateUtil.ToString(DateUtil.GetUTCTime(),"yyyyMMdd");
                if(device.Notes != null)
                    mReport.Notes = device.Notes;
                if(device.Description != null)
                    mReport.Description = device.Description;

                mReport.SamplingInterval = device.SaveInterval; //device.SamplingInterval;
                mReport.LT = device.LT;
                mReport.HT = device.HT;
            }

            if(device.SavaCount == -1000 || device.SavaCount == 0){
                Toast.makeText(GetDataActivity.this, "Data record is empty! Without extracting", Toast.LENGTH_SHORT).show();
                finish();
            }

            _IsSync = true;
            _ConfigService.Sync(true);

            if(_ProgressDialog!=null && _ProgressDialog.isShowing())
                _ProgressDialog.dismiss();

            _ProgressDialog = new ProgressDialog(this).show(this,"","Fetching data,please wait...",true,false,null);
            _ProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(GetDataActivity.this, "Extract the data has been canceled!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            _ProgressDialog.show();
            _ProgressDialog.setProgress(0);
        }catch (Exception ex){
            _IsSync = false;
            Toast.makeText(GetDataActivity.this, "Extract data during abnormal!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    protected void ShowData(){
        try {

            //上一步骤状态关闭
            _IsSync = false;
            //_ConfigService.Sync(false);
            _ConfigService.Dispose();

            if(_ProgressDialog!=null && _ProgressDialog.isShowing())
                _ProgressDialog.dismiss();

            if(Buffer == null || Buffer.size() == 0)
                return;

            _ProgressDialog = new ProgressDialog(this).show(this,"","please wait...",true,false,null);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        double totalTemperature = 0;int totalTemperature_count = 0;
                        double totalHumidity = 0;int totalHumidity_count = 0;
                        while (Buffer.size() > 0) {
                            Device device = new Device();
                            device.HardwareModel = HardwareModel;
                            device.Firmware = Firmware;
                            device.fromNotificationData(Buffer.remove());
                            if(device !=null && device.UTCTime != null){
                                if(device.Temperature != -1000) {
                                    totalTemperature += device.Temperature;
                                    totalTemperature_count ++;
                                    if (mReport.MaxTemp == -1000)
                                        mReport.MaxTemp = device.Temperature;
                                    if (mReport.MinTemp == -1000)
                                        mReport.MinTemp = device.Temperature;

                                    if (device.Temperature > mReport.MaxTemp)
                                        mReport.MaxTemp = device.Temperature;
                                    if (device.Temperature < mReport.MinTemp)
                                        mReport.MinTemp = device.Temperature;
                                }
                                if(device.Humidity != -1000) {
                                    totalHumidity += device.Humidity;
                                    totalHumidity_count ++;
                                    if (mReport.MaxHumidity == -1000)
                                        mReport.MaxHumidity = device.Humidity;
                                    if (mReport.MinHumidity == -1000)
                                        mReport.MinHumidity = device.Humidity;

                                    if (device.Humidity > mReport.MaxHumidity)
                                        mReport.MaxHumidity = device.Humidity;
                                    if (device.Humidity < mReport.MinHumidity)
                                        mReport.MinHumidity = device.Humidity;
                                }

                                ReportData reportData = new ReportData(device);
                                mReport.Data.add(reportData);
                            }
                        }

                        mReport.DataCount = mReport.Data.size();
                        if(mReport.Data.size() > 0 ) {
                            if(totalTemperature_count > 0)
                                mReport.AvgTemp = Math.round(totalTemperature/ totalTemperature_count);
                            if(totalHumidity_count > 0)
                                mReport.AvgHumidity = Math.round(totalHumidity / totalHumidity_count);
                            mReport.BeginTime = mReport.Data.get(0).RecordTime;
                            mReport.EndTime = mReport.Data.get(mReport.Data.size() - 1).RecordTime;
                        }

                        mReport.Generate();

                        GetDataActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String printText = "";
                                for (int i = 0; i < mReport.Data.size() && i < 25; i++) {
                                    ReportData item = mReport.Data.get(i);
                                    printText += (i + 1) + "、";
                                    double tzone = DateUtil.GetTimeZone() / 60.0;
                                    printText += DateUtil.ToString(DateUtil.DateAddHours(item.RecordTime,tzone), "yyyy-MM-dd HH:mm:ss")+" ";
                                    if(item.Temperature != -1000)
                                        printText += "Temperature:" + item.Temperature+"℃";
                                    else
                                        printText += "Temperature:--℃";
                                    if(item.Humidity != -1000)
                                        printText += "Humidity:" + item.Humidity+"%";
                                    else
                                        printText += "Humidity:--%";
                                    printText += "\n\n";

                                }
                                txtPrint.setText(printText + "......");

                                _ProgressDialog.dismiss();

                            }
                        });

                    }catch (Exception ex){
                        _ProgressDialog.dismiss();
                    }
                }
            }).start();

        }catch (Exception ex){
            Toast.makeText(GetDataActivity.this, " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if(mBroadcastService!=null)
                mBroadcastService.StopScan();
            if(_ConfigService!=null)
                _ConfigService.Dispose();
        }catch (Exception ex){}
        super.onDestroy();
    }

    public ILocalBluetoothCallBack _LocalBluetoothCallBack = new ILocalBluetoothCallBack() {
        @Override
        public void OnEntered(BLE ble) {
            final Device device = new Device();
            device.fromScanData(ble);
            if(device.SN!=null && device.SN.equals(SN)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Connect(device);
                    }
                });
            }
        }

        @Override
        public void OnUpdate(BLE ble) {

        }

        @Override
        public void OnExited(BLE ble) {

        }

        @Override
        public void OnScanComplete() {

        }
    };

    public IConfigCallBack _IConfigCallBack = new IConfigCallBack() {
        @Override
        public void OnReadConfigCallBack(boolean status, final HashMap<String, byte[]> uuids) {
            if(status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Device device = _ConfigService.GetCofing(uuids);
                        SyncData(device);
                    }
                });
            }
        }

        @Override
        public void OnWriteConfigCallBack(boolean status) {

        }

        @Override
        public void OnConnected() {

        }

        @Override
        public void OnDisConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(_IsSync) {
                        if (_ProgressDialog != null && _ProgressDialog.isShowing())
                            _ProgressDialog.dismiss();
                        Toast.makeText(GetDataActivity.this, "Disconnected!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }

        @Override
        public void OnServicesed(List<BLEGattService> services) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _ConfigService.CheckToken(Password);
                }
            });
        }

        @Override
        public void OnReadCallBack(UUID uuid, byte[] data) {

        }

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

        @Override
        public void OnReceiveCallBack(UUID uuid, byte[] data) {
            try {
                int serial = Integer.parseInt(StringConvertUtil.bytesToHexString(BinaryUtil.CloneRange(data, data.length - 3, 2)),16); //data[data.length - 3] * 256 + data[data.length - 2];
                String crc = StringConvertUtil.bytesToHexString(BinaryUtil.CloneRange(data, data.length - 1, 1));
                String checksum = CRC(BinaryUtil.CloneRange(data, 0, data.length - 1));

                if (!checksum.equals(crc)) {
                    Log.e("SyncActivity", "SN：" + serial + " crc:" + crc + " error");
                    return;
                }

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
                        int progress = syncProgress;
                        if(_SyncIndex >= _SyncCount){
                            _SyncIndex = _SyncCount;
                            progress = 100;
                            isComplete = true;
                        }
                        _SyncProgress = progress;
                        if(isComplete) {
                            ShowData();
                        }
                    }
                });

            }catch (Exception ex){}
        }
    };

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


}
