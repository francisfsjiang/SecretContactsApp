package com.never.secretcontacts.util;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class PhoneReceiver extends BroadcastReceiver {
    private static boolean incomingFlag = false;

    private TelephonyManager tm_;
    private Context context_;
    //    private String incomingNumber;
    @Override
    public void onReceive(Context context, Intent intent) {
        tm_ = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        context_ = context;
        //拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            incomingFlag = false;
            final String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.i("PhoneReceiver", "phoneNum: " + phoneNum);
        } else {
            tm_.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
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
                    startInfoWindow(context_, incomingNumber);
                    if (incomingNumber.equals("15801310352")) {
                        try {
//                            tm_.getClass().getMethod("silenceRinger").invoke(tm_);
//                            tm_.getClass().getMethod("endCall").invoke(tm_);
                            Log.i("PhoneReceiver", "CALL IN HANG UP :" + incomingNumber);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
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
                    endInfoWindow();
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

    private WindowManager wm_;
    private TextView tv_;

    public void startInfoWindow(Context context, String phone) {
//        wm_ = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
//        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.format = PixelFormat.RGBA_8888;
//        tv_ = new TextView(context);
//        tv_.setText("这是悬浮窗口，来电号码：" + phone);
//        wm_.addView(tv_, params);

        Toast.makeText(context,"onCreate", Toast.LENGTH_LONG).show();
        tv_ = new TextView(context);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Load Average");
        WindowManager wm_ = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        wm_.addView(tv_ , params);
    }

    public void endInfoWindow() {
        if(wm_ != null){
            wm_.removeView(tv_);
        }
    }

}