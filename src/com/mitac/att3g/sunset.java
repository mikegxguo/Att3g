package com.mitac.att3g;

import com.quectel.modemtool.ModemTool;
import com.quectel.modemtool.NvConstants;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
//import android.widget.Button;
import android.util.Log;
import android.os.SystemProperties;
import android.content.Context;
//import android.os.PowerManager;
//import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.FileWriter;
//import android.os.RemoteException;

public class sunset extends Activity {
    private static final String TAG = "Sunset";
    //private boolean mGsmDisabled = false;
    //private Button  mEnableGsmBtn;
    //private Button  mDisableGsmBtn;
    private ModemTool mTool;
    //private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.sunset_layout);
        //mContext = this;
        //mEnableGsmBtn = (Button)findViewById(R.id.enable_gsm);
        //mDisableGsmBtn = (Button)findViewById(R.id.disable_gsm);

        log("onCreate()");

        String sc600_sku = SystemProperties.get("ro.boot.sc600_sku");
        String project = SystemProperties.get("ro.product.name");
        /*
        if(sc600_sku.contains("EM") && project.contains("gemini")) {
            mTool = new ModemTool();

            mGsmDisabled = IsGsmDisabled();
            if(mGsmDisabled == true) {
                mEnableGsmBtn.setEnabled(true);
                mDisableGsmBtn.setEnabled(false);
            } else {
                mEnableGsmBtn.setEnabled(false);
                mDisableGsmBtn.setEnabled(true);
            }
        } else {
            mEnableGsmBtn.setEnabled(false);
            mDisableGsmBtn.setEnabled(false);
        }
        */
        if(sc600_sku.contains("NA") && project.contains("gemini")) {
            mTool = new ModemTool();
            handleAtt3gSunset();
            WriteDataLog("/mnt/sdcard/ATT_3G_Sunset.txt", "AT&T 3G sunset");
        }
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        log("onPause()");
    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume()");
    }


    private String sendGetAT(String atCommand, String prefix) {
        String content = null;
        BufferedReader br = null;
        try {
            //ATInterface atInterface = getATInterface();
            //String result = atInterface.sendAT(atCommand);
            String result = mTool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND, atCommand);
            //Log.d(TAG, "sendGetAT : atCommand=" + atCommand + ", prefix=" + prefix + ", result=" + result);
            if(result != null && result.contains("OK")) {
                br = new BufferedReader(new StringReader(result));
                String line;
                while((line = br.readLine()) != null) {
                    if(line.contains(prefix)) {
                        content = line.substring(prefix.length());
                        //content = content.replace("\"", "");
                        break;
                    }
                }
            } else if(result != null && result.contains("ERROR")) {
                content = "ERROR";
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return content;
    }

    public boolean handleAtt3gSunset() {
        String prefix = "+QNVFR: ";
        String val = null;
        String UE_USAGE_SETTING_R = "AT+QNVFR=\"/nv/item_files/modem/mmode/ue_usage_setting\"";
        String UE_USAGE_SETTING_W = "AT+QNVFW=\"/nv/item_files/modem/mmode/ue_usage_setting\",01";
        String IMS_ENABLE_R = "AT+QNVFR=\"/nv/item_files/ims/IMS_enable\"";
        String IMS_ENABLE_W = "AT+QNVFW=\"/nv/item_files/ims/IMS_enable\",00";
        String SMS_MANDATORY_R = "AT+QNVFR=\"/nv/item_files/modem/mmode/sms_mandatory\"";
        String SMS_MANDATORY_W = "AT+QNVFW=\"/nv/item_files/modem/mmode/sms_mandatory\",00";
        String RESET_MODEM = "AT+QCFG=\"reset\"";
        //check UE_USAGE_SETTING
        val = sendGetAT(UE_USAGE_SETTING_R, prefix);
        if(val.contains("00") || val.contains("ERROR")) { //voice centric
            Log.d(TAG, "Voice centric, now change it to data centric for AT&T 3G sunset");
            sendAT(UE_USAGE_SETTING_W);
        } else {
            Log.d(TAG, "Data centric");
        }
        //check IMS_ENABLE
        val = sendGetAT(IMS_ENABLE_R, prefix);
        if(val.contains("01")) {
            Log.d(TAG, "IMS is enabled, now disable it for AT&T 3G sunset.");
            sendAT(IMS_ENABLE_W);
        } else {
            Log.d(TAG, "IMS is disabled");
        }
        //check SMS mandatory
        val = sendGetAT(SMS_MANDATORY_R, prefix);
        if(val.contains("01")) {
            Log.d(TAG, "SMS MANDATORY is enabled, now disable it for AT&T 3G sunset");
            sendAT(SMS_MANDATORY_W);
        } else {
            Log.d(TAG, "SMS MANDATORY is disabled");
        }
        //reset modem
        sendAT(RESET_MODEM);
        return true;
    }
/*
    private void reboot() {
        try {
            Thread.sleep(3000);
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            pm.reboot("Control GSM function");
        } catch(Exception e) {
            Log.d(TAG, "reboot error", e);
        }
    }
*/
    private boolean sendAT(String cmd) {
        boolean res = false;
        try {
            String result = mTool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND, cmd);
            //Log.d(TAG, "sendAT : cmd = " + cmd + ", result = " + result);
            if (result != null && result.contains("OK")) {
                res = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "SendAT Error", e);
        }
        return res;
    }

/*
    public String sendAtCommand(String atCommand) {
        String result = null;
        try {
            ATInterface atInterface = getATInterface();
            result = atInterface.sendAT(atCommand);
            Log.d(TAG, "sendAtCommand : cmd=" + atCommand + ", result=" + result);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString(), e);
        } catch (Exception e1) {
            Log.e(TAG, e1.toString(), e1);
        }
        return result;
    }
*/

    //Disable GSM since it may trigger UVLO
/*    private void DisableGsm() {
        boolean res = false;
        String sc600_sku = SystemProperties.get("ro.boot.sc600_sku");
        if(sc600_sku.contains("EM")) {
            res = sendAT("at+qnvw=1877,0,\"0000C00600000200\"");
            res = sendAT("at+qnvr=1877,0");
            if(res) {
                Log.d(TAG, "Disable GSM since it may trigger UVLO!");
            }
            SystemProperties.set("persist.sys.gsm.status", "0");
        }
    }
*/
    //Restore GSM since it may be disabled by the experiment(UVLO).
/*    private void EnableGsm() {
        boolean res = false;
        String sc600_sku = SystemProperties.get("ro.boot.sc600_sku");
        if(sc600_sku.contains("EM")) {
            res = sendAT("at+qnvw=1877,0,\"8003E80600000200\"");
            res = sendAT("at+qnvr=1877,0");
            if(res) {
                Log.d(TAG, "Restore GSM since it may be disabled by the experiment(UVLO).");
            }
            SystemProperties.set("persist.sys.gsm.status", "1");
        }
    }

    private boolean IsGsmDisabled() {
        boolean ret = false;
        String strVal = null;
        try {
            String cmd = "at+qnvr=1877,0";
            String result = mTool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND, cmd);
            Log.d(TAG, "sendAT : cmd = " + cmd + "\n result = " + result);
            if (result != null && result.contains("OK")) {
                String prefix = "+QNVR: \"";
                int idx = result.indexOf(prefix);
                if(idx >= 0) {
                    idx += prefix.length();
                    strVal = result.substring(idx, idx+16);
                    Log.d(TAG, "NV item: "+strVal);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "SendAT Error", e);
        }
        if("0000C00600000200".equals(strVal)) {
            ret = true; //GSM is disabled
        } else {
            ret = false; //GSM keeps in original settings
        }
        SystemProperties.set("persist.sys.gsm.status", ret?"0":"1");
        return ret;
    }
*/
//    public void onEnableGsm(View view) {
//        if(mGsmDisabled == true) {
//            mGsmDisabled = false;
//            EnableGsm();
//            mEnableGsmBtn.setEnabled(false);
//            mDisableGsmBtn.setEnabled(true);
//            SystemProperties.set("persist.sys.gsm.manual", "1");
//            reboot();
//        }
//        //FIXME: ONLY FOR TEST
//        /*
//        Intent intent = new Intent();
//        intent.setAction(ATService.ACTION_DISABLE_GSM);
//        mContext.sendBroadcast(intent);
//        */
//        return ;
//    }
/*
    public void onDisableGsm(View view) {
        if(mGsmDisabled == false) {
            mGsmDisabled = true;
            DisableGsm();
            mEnableGsmBtn.setEnabled(true);
            mDisableGsmBtn.setEnabled(false);
            SystemProperties.set("persist.sys.gsm.manual", "1");
            reboot();
        }
        return ;
    }
*/
    public void WriteDataLog(String strFilePath, String strlog) {
        String Filename = strFilePath;

        String strline = strlog + "\n\r";
        FileWriter fw = null;
        try {
            fw = new FileWriter(Filename, true);
            //fw.append(strline);
            fw.write(strline);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void log(String msg) {
        Log.d(TAG, "GSM: " + msg);
    }

}
