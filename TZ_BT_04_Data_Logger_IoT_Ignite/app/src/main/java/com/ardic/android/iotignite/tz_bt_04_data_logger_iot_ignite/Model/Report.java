package com.ardic.android.iotignite.tz_bt_04_data_logger_iot_ignite.Model;

import com.TZONE.Bluetooth.Utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class Report {


    public String SN;
    /**
     * Report Name
     */
    public String Name;
    public String Notes;
    public String Description;
    public int SamplingInterval;
    public int Battery;

    /**
     * Device Type
     */
    public String ProductType;
    public String FirmwareVersion;
    public double LT;
    public double HT;
    public int DataCount;
    public double MaxTemp;
    public double MinTemp;
    public double AvgTemp;
    public double MaxHumidity;
    public double MinHumidity;
    public double AvgHumidity;
    public Date BeginTime;
    public Date EndTime;

    public List<ReportData> Data;

    /*******************/


    public String ReportID;
    public String Token ;
    public Date CreateTime;

    public Report(){
        SamplingInterval = -1000;
        Battery = -1000;
        LT = -1000;
        HT = -1000;
        ProductType = "BT04";
        FirmwareVersion = "v01";
        DataCount = 0;
        MaxTemp = -1000;
        MinTemp = -1000;
        AvgTemp = -1000;
        MaxHumidity = -1000;
        MinHumidity = -1000;
        AvgHumidity = -1000;
        Data = new ArrayList<>();

        this.Name = "";
        this.Notes = "";
        this.Description = "";
    }

    /**
     * Generate Report
     */
    public boolean Generate(){
        try {
            this.ReportID = this.SN + "" + DateUtil.ToString(DateUtil.GetUTCTime(),"yyyyMMddHHmmss") + "" + ((new Random()).nextInt(99-10+1)+10);
            this.Token = ((new Random()).nextInt(9999-1000+1)+1000)+"";
            this.CreateTime = DateUtil.GetUTCTime();


            //Year after year treatment
            if(this.Data.size() > 0){
                int len = this.Data.size();
                while (this.EndTime.getTime() < this.BeginTime.getTime()){
                    boolean flag = false;
                    ReportData lastRd = null;
                    for (int i = 1; i <= len; i++) {
                        ReportData rd = this.Data.get(len - i);

                        //Locate the data before the span
                        if(lastRd != null
                                && rd.RecordTime.getYear() == lastRd.RecordTime.getYear()
                                && Math.abs(rd.RecordTime.getMonth() - lastRd.RecordTime.getMonth()) == 11){
                            //Correction time
                           /* rd.RecordTime = new Date(rd.RecordTime.getYear() - 1
                                    ,rd.RecordTime.getMonth()
                                    ,rd.RecordTime.getDay()
                                    ,rd.RecordTime.getHours()
                                    ,rd.RecordTime.getMinutes()
                                    ,rd.RecordTime.getSeconds());*/
                            rd.RecordTime = DateUtil.ToData((Integer.parseInt(DateUtil.ToString(rd.RecordTime, "yyyy")) - 1) + "-" +  DateUtil.ToString(rd.RecordTime, "MM-dd HH:mm:ss"),"yyyy-MM-dd HH:mm:ss");
                            flag = true;
                            continue;
                        }
                        lastRd = rd;
                    }
                    this.BeginTime = this.Data.get(0).RecordTime;
                    this.EndTime = this.Data.get(this.Data.size() - 1).RecordTime;
                    if(!flag)
                        break;
                }
            }

            return true;
        }catch (Exception ex){
            return false;
        }
    }



}
