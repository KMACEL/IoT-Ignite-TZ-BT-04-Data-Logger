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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.TZONE.Bluetooth.AppBase;
import com.TZONE.Bluetooth.BLE;
import com.TZONE.Bluetooth.BLEGattService;
import com.TZONE.Bluetooth.IConfigCallBack;
import com.TZONE.Bluetooth.ILocalBluetoothCallBack;
import com.TZONE.Bluetooth.Temperature.BroadcastService;
import com.TZONE.Bluetooth.Temperature.ConfigService;
import com.TZONE.Bluetooth.Temperature.Model.CharacteristicHandle;
import com.TZONE.Bluetooth.Temperature.Model.CharacteristicType;
import com.TZONE.Bluetooth.Temperature.Model.Device;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;


public class SettingActivity extends Activity {

    private EditText txtSN;
    private EditText txtToken;
    private Spinner txtTransmitPower;
    private EditText txtSaveInterval;
    private EditText txtLT;
    private EditText txtHT;
    private TextView labTrip;
    private Switch swTrip;
    private Button btnSubmit;

    private ProgressDialog _ProgressDialog;
    private Dialog _AlertDialog;

    private String SN = "";
    private String Token = "000000";
    private String HardwareModel = "3A01";
    private String Firmware = "";
    private Device _Device;
    private BluetoothAdapter _BluetoothAdapter;
    private BroadcastService _BroadcastService;
    private ConfigService _ConfigService;
    private boolean _IsInit = false;
    private boolean _IsScanning = false;
    private boolean _IsConnecting = false;
    private boolean _IsReading = false;
    private boolean _IsWriting = false;

    public String[] TransmitPower_Str = new String[]{"4dBm","0dBm","-4dBm","-8dBm","-12dBm","-16dBm","-20dBm","-30dBm"};
    public String[] TransmitPower_Int = new String[]{"4","0","-4","-8","-12","-16","-20","-30"};
    private ArrayAdapter<String> adapter_TransmitPower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        txtSN = (EditText)findViewById(R.id.txtSN);
        txtToken = (EditText)findViewById(R.id.txtToken);
        txtTransmitPower = (Spinner)findViewById(R.id.txtTransmitPower);
        txtSaveInterval = (EditText)findViewById(R.id.txtSaveInterval);
        txtLT = (EditText)findViewById(R.id.txtLT);
        txtHT = (EditText)findViewById(R.id.txtHT);
        labTrip = (TextView)findViewById(R.id.labTrip);
        swTrip = (Switch)findViewById(R.id.swTrip);
        btnSubmit = (Button)findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WirteConfig();
            }
        });
        InitControl();

        InputToken();
    }

    protected void InitControl(){
        try {
            txtToken.setText("000000");

            adapter_TransmitPower = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,TransmitPower_Str);
            adapter_TransmitPower.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            txtTransmitPower.setAdapter(adapter_TransmitPower);

            swTrip.setChecked(false);
            txtSN.setEnabled(false);
            btnSubmit.setEnabled(false);

        }catch (Exception ex){
            finish();
        }
    }

    //Input Password
    public void InputToken(){
        final LinearLayout layout = new LinearLayout(SettingActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText txtSN2 = new EditText(SettingActivity.this);
        final EditText txtToken2 = new EditText(SettingActivity.this);
        txtSN2.setHint("SN");
        layout.addView(txtSN2);
        layout.addView(txtToken2);
        txtToken2.setText(Token+"");
        new AlertDialog.Builder(SettingActivity.this)
                .setTitle("Please enter SN and password")
                .setView(layout)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO 自动生成的方法存根
                        try {
                            SN = txtSN2.getText().toString();
                            Token = txtToken2.getText().toString();

                            if(SN.isEmpty())
                                finish();
                        } catch (Exception ex) {
                            _AlertDialog = new AlertDialog.Builder(SettingActivity.this).
                                    setTitle("Message")
                                    .setMessage("SN or Password is error!")
                                    .setPositiveButton("Confirm", null)
                                    .show();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Scan();
                            }
                        });
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    /**
     * Scan
     */
    protected void Scan(){
        try {
            if(SN == null || SN.equals(""))
                finish();

            if(_BroadcastService == null){
                _BroadcastService = new BroadcastService();
            }
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            _BluetoothAdapter = bluetoothManager.getAdapter();
            if(_BroadcastService.Init(_BluetoothAdapter,_LocalBluetoothCallBack)){
                _IsInit = true;
            }else {
                _IsInit = false;
                Toast.makeText(SettingActivity.this, "Native Bluetooth hardware or driver was not found!", Toast.LENGTH_SHORT).show();
                return;
            }

            if(_IsScanning)
                return;

            _IsScanning = true;
            _BroadcastService.StartScan();

            if(_ProgressDialog!=null && _ProgressDialog.isShowing())
                _ProgressDialog.dismiss();
            _ProgressDialog = new ProgressDialog(this).show(this,"","Searching for devices...",true,true,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(SettingActivity.this, "Could not find the device!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(15000);
                        if(_IsScanning){
                            _BroadcastService.StopScan();
                            _ProgressDialog.cancel();
                            _IsScanning = false;
                        }
                    }catch (Exception ex){}
                }
            }).start();


        }catch (Exception ex){
            _IsInit = false;
            _IsScanning = false;
            Toast.makeText(SettingActivity.this, "Could not find the device!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Connect
     */
    protected void Connect(Device device){
        try {
            if(_IsConnecting)
                return;

            _BroadcastService.StopScan();
            _IsScanning = false;

            HardwareModel = device.HardwareModel;
            Firmware = device.Firmware;


            _IsConnecting = true;
            if(_ConfigService != null){
                _ConfigService.Dispose();
            }
            _ConfigService = new ConfigService(_BluetoothAdapter,this,device.MacAddress,30000,_IConfigCallBack);

            if(_ProgressDialog!=null && _ProgressDialog.isShowing())
                _ProgressDialog.dismiss();
            _ProgressDialog = new ProgressDialog(this).show(this,"","Connecting...",true,true,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(SettingActivity.this, "Unable to connect the device!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(30000);
                        if(_IsConnecting) {
                            _ProgressDialog.cancel();
                            _IsConnecting = false;
                        }
                    }catch (Exception ex){}
                }
            }).start();
        }catch (Exception ex){
            _IsConnecting = false;
            Toast.makeText(SettingActivity.this, "Unable to connect the device!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * ReadConfig
     */
    protected void ReadConfig(){
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
                    Toast.makeText(SettingActivity.this, "Can not read the device parameters!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SettingActivity.this, "Can not read the device parameters!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Show Config
     * @param device
     */
    protected void ShowConfig(final Device device){
        try {
            _IsReading = false;
            if(_ProgressDialog!=null && _ProgressDialog.isShowing())
                _ProgressDialog.dismiss();

            if(device!=null){
                if(device.SN!=null)
                    txtSN.setText(device.SN+"");
                txtTransmitPower.setSelection(GetTransmitPowerIndex(device.TransmitPower));
                txtToken.setText(Token);

                txtSaveInterval.setText(device.SaveInterval+"");
                txtLT.setText(device.LT+"");
                txtHT.setText(device.HT+"");
                if(HardwareModel.equals("3901")) {
                    labTrip.setText("Travel Records");
                    if (device.TripStatus == 0)
                        swTrip.setChecked(false);
                    else
                        swTrip.setChecked(true);
                }else {
                    swTrip.setChecked(false);
                    labTrip.setText("Memory Clear");
                }
                _Device = device;
            }
        }catch (Exception ex){
            Toast.makeText(SettingActivity.this, "Reading device parameters failed!" + " ex:"+ex.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Wirte Config
     */
    protected void WirteConfig(){
        try{

            if(!_ConfigService.IsConnected) {
                Toast.makeText(SettingActivity.this, "Disconnected!", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(false);
                return;
            }
            if(_IsWriting)
                return;

            if(_Device == null)
                return;

            Device newDevice =_Device;
            newDevice.HardwareModel = HardwareModel;
            newDevice.Firmware = Firmware;
            newDevice.SN = txtSN.getText().toString();

            if((newDevice.SN == null && !newDevice.SN.isEmpty())
                    && newDevice.SN.length() != 8){
                _AlertDialog = new AlertDialog.Builder(this).
                        setTitle("Message")
                        .setMessage("SN is error !")
                        .setPositiveButton("Confirm", null)
                        .show();
                return;
            }
            newDevice.Token = txtToken.getText().toString();

            if((newDevice.Token != null && !newDevice.Token.isEmpty())
                    && (newDevice.Token.length() != 6 || !Pattern.matches("\\d+", newDevice.Token))){
                _AlertDialog = new AlertDialog.Builder(this).
                        setTitle("Message")
                        .setMessage("Password is error. Length of 6 characters [0-9]")
                        .setPositiveButton("Confirm", null)
                        .show();
                return;
            }

            newDevice.TransmitPower = GetTransmitPower(txtTransmitPower.getSelectedItemPosition());

            try {
                newDevice.SaveInterval = Integer.parseInt(txtSaveInterval.getText().toString());
                if((newDevice.SaveInterval < 10 || newDevice.SaveInterval > 3600)
                        || (newDevice.SaveInterval2 < 10 || newDevice.SaveInterval2 > 3600) ){

                    _AlertDialog = new AlertDialog.Builder(this).
                            setTitle("Message")
                            .setMessage("Storage interval is error!" + " [10,3600]")
                            .setPositiveButton("Confirm", null)
                            .show();
                    return;
                }
            }catch (Exception ex){
                _AlertDialog = new AlertDialog.Builder(this).
                        setTitle("Message")
                        .setMessage("Storage interval is error!")
                        .setPositiveButton("Confirm", null)
                        .show();
                return;
            }

            try {
                newDevice.LT = Double.parseDouble(txtLT.getText().toString());
                newDevice.HT = Double.parseDouble(txtHT.getText().toString());
                //newDevice.UTCTime=;
                if(HardwareModel.equals("3A01")) {
                    if ((newDevice.LT < -20 || newDevice.LT > 60)
                            || (newDevice.HT < -20 || newDevice.HT > 60)) {

                        _AlertDialog = new AlertDialog.Builder(this).
                                setTitle("Message")
                                .setMessage("Alarm is error!" + " [-20,60]")
                                .setPositiveButton("Confirm", null)
                                .show();
                        return;
                    }
                }else {
                    if ((newDevice.LT < -20 || newDevice.LT > 100)
                            || (newDevice.HT < -20 || newDevice.HT > 100)) {

                        _AlertDialog = new AlertDialog.Builder(this).
                                setTitle("Message")
                                .setMessage("Alarm is error!" + " [-20,100]")
                                .setPositiveButton("Confirm", null)
                                .show();
                        return;
                    }
                }

                if(newDevice.LT > newDevice.HT){
                    _AlertDialog = new AlertDialog.Builder(this).
                            setTitle("Message")
                            .setMessage("Alarm is error!")
                            .setPositiveButton("Confirm", null)
                            .show();
                    return;
                }
            }catch (Exception ex){
                _AlertDialog = new AlertDialog.Builder(this).
                        setTitle("Message")
                        .setMessage("Alarm is error!")
                        .setPositiveButton("Confirm", null)
                        .show();
                return;
            }

            if(swTrip.isChecked())
                newDevice.TripStatus = 1;
            else
                newDevice.TripStatus = 0;

            _IsWriting = true;
            btnSubmit.setEnabled(false);
            if(HardwareModel.equals("3901"))
                _ConfigService.WriteConfig_BT04(newDevice);
            else
                _ConfigService.WriteConfig_BT05(newDevice);

            if(_ProgressDialog!=null && _ProgressDialog.isShowing())
                _ProgressDialog.dismiss();
            _ProgressDialog = new ProgressDialog(this).show(this,"","Saving device parameters...",true,false,null);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(15000);
                        if(_IsWriting){
                            _ProgressDialog.dismiss();
                            btnSubmit.setEnabled(true);
                            _IsWriting = false;
                        }
                    }catch (Exception ex){}
                }
            }).start();

        }catch (Exception ex){
            Log.e("SettingActivity", "WirteConfig:" + ex.toString());
            _AlertDialog = new AlertDialog.Builder(this).
                    setTitle("Message")
                    .setMessage("Parameter is error!")
                    .setPositiveButton("Confirm", null)
                    .show();
            btnSubmit.setEnabled(true);
            _IsWriting = false;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if(_BroadcastService!=null)
                _BroadcastService.StopScan();
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
                        ShowConfig(device);
                    }
                });
            }
        }

        @Override
        public void OnWriteConfigCallBack(final boolean status) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(_IsWriting) {
                        if (_ProgressDialog != null && _ProgressDialog.isShowing())
                            _ProgressDialog.dismiss();
                        _IsWriting = false;

                        if (status) {
                            _AlertDialog = new AlertDialog.Builder(SettingActivity.this).
                                    setTitle("Message")
                                    .setMessage("Save the configuration successful!")
                                    .setPositiveButton("Confirm", null)
                                    .show();
                            if(!AppBase.IsFactory) {
                                btnSubmit.setEnabled(false);
                                btnSubmit.setText("Saved Settings");
                                _ConfigService.Dispose();
                            }else {
                                btnSubmit.setEnabled(true);
                            }
                        } else {
                            _AlertDialog = new AlertDialog.Builder(SettingActivity.this).
                                    setTitle("Message")
                                    .setMessage("Save configuration failed!")
                                    .setPositiveButton("Confirm", null)
                                    .show();
                            if(_ConfigService.IsConnected) {
                                btnSubmit.setEnabled(true);
                                btnSubmit.setText("Save Settings");
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void OnConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnSubmit.setEnabled(true);
                }
            });
        }

        @Override
        public void OnDisConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnSubmit.setEnabled(false);
                    //Toast.makeText(SettingActivity.this, getString(R.string.lan_151), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void OnServicesed(List<BLEGattService> services) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _ConfigService.CheckToken(Token);
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
                        if(isSuccess)
                            ReadConfig();
                    }
                });
            }
        }

        @Override
        public void OnReceiveCallBack(UUID uuid, byte[] data) {

        }
    };

    public int GetTransmitPowerIndex(int value){
        try {
            for (int i = 0; i < TransmitPower_Int.length; i++) {
                if(value >= Integer.parseInt(TransmitPower_Int[i])){
                    return i;
                }
            }
        }catch (Exception ex){
            Log.e("GetTransmitPowerIndex", ex.toString());
        }
        return 0;
    }

    public int GetTransmitPower(int index){
        try {
            return Integer.parseInt(TransmitPower_Int[index]);
        }catch (Exception ex){
            Log.e("GetTransmitPower",ex.toString());
            return -20;
        }
    }



    /**
     * String To int
     * @param res
     * @return
     */
    private int GetStringToInt(String res){
        try{
            return Integer.parseInt(res);
        }catch (Exception ex){
            return -1;
        }
    }
}
