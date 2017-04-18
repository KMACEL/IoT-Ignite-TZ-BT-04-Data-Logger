package com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite.Model;

import com.TZONE.Bluetooth.Temperature.Model.Device;
import com.TZONE.Bluetooth.Utils.DateUtil;

import java.util.Date;

public class ReportData {

    public String SN;

    public double Temperature;

    public double Humidity;

    public Date RecordTime;

    public Date ServerTime;

    public ReportData(){}
    public ReportData(Device device){
        this.SN = device.SN;
        this.Temperature = device.Temperature;
        this.Humidity = device.Humidity;
        this.RecordTime = device.UTCTime;
        this.ServerTime = DateUtil.GetUTCTime();
    }


}
