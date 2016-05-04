package com.never.secretcontacts.util;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class PhoneReceiver extends BroadcastReceiver {
    private static boolean incomingFlag = false;

    private Method end_call_;
    private Method silence_call_;
    private Object iTelephony;
    //    private String incomingNumber;
    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

        try {

            Method m1 = tm.getClass().getDeclaredMethod("getITelephony");
            m1.setAccessible(true);
            iTelephony = m1.invoke(tm);

            silence_call_ = iTelephony.getClass().getDeclaredMethod("silenceRinger");
            end_call_ = iTelephony.getClass().getDeclaredMethod("endCall");

        }
        catch (Exception e) {
            Log.i("PhoneReceiver", "get method failed");
        }
        //拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            incomingFlag = false;
            final String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i("PhoneReceiver", "phoneNum: " + phoneNum);
        } else {
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
    final PhoneStateListener listener=new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            Log.i("PhoneReceiver", "State " + state);
            switch(state){
                //电话等待接听
                case TelephonyManager.CALL_STATE_RINGING:
                    incomingFlag = true;
                    Log.i("PhoneReceiver", "CALL IN RINGING :" + incomingNumber);
                    if (incomingNumber.equals("15801310352")) {
                        try {
//                            end_call_.invoke(iTelephony);
                            silence_call_.invoke(iTelephony);
                            Log.i("PhoneReceiver", "CALL IN HANG UP :" + incomingNumber);
                        }
                        catch (Exception e) {
                            Log.i("PhoneReceiver", "endcall failed");
                        }
                    }
                    break;
                //电话接听
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (incomingFlag) {
                        Log.i("PhoneReceiver", "CALL IN ACCEPT :" + incomingNumber);
                    }
                    else {
                        Log.i("PhoneReceiver", "CALL OUT ACCEPT :" + incomingNumber);
                    }
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    if (incomingFlag) {
                        Log.i("PhoneReceiver", "CALL IN IDLE");
                    }
                    else {
                        Log.i("PhoneReceiver", "CALL OUT IDLE");
                    }
                    break;
            }
        }
    };

}