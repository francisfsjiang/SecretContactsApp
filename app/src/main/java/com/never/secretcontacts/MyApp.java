package com.never.secretcontacts;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.never.secretcontacts.util.Contact;
import com.never.secretcontacts.util.ContactsManager;
import com.never.secretcontacts.util.HarassingCallManager;
import com.never.secretcontacts.util.SecretKeyManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;


public class MyApp extends Application{

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static String URL_SITE = "https://sc.404notfound.top/";

    public static String URL_LOGIN = URL_SITE + "api/login";
    public static String URL_REGISTER = URL_SITE + "api/register";
    public static String URL_POLLING = URL_SITE + "api/polling";
    public static String URL_KEY = URL_SITE + "api/key";
    public static String URL_CONTACTS = URL_SITE + "api/contacts";
    public static String URL_HARASSING = URL_SITE + "api/harassing";
    public static String URL_UPLOAD_HARASSING = URL_SITE + "api/upload_harassing";

    private static String auth_id_;
    private static String auth_key_;

    private static SharedPreferences shared_preference_;

    public static ContactsManager contacts_manager_;

    public static SecretKeyManager key_manager_;

    public static HarassingCallManager harassing_call_manager_;


    private static boolean incomingFlag = false;
    private BroadcastReceiver phone_receiver_ = new BroadcastReceiver() {
        private TelephonyManager tm_;
        private WindowManager window_manager_;
        private TextView window_alert_view_;
        @Override
        public void onReceive(Context context, Intent intent) {
            tm_ = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
//        context_ = MyApp.getAppContext();
            window_manager_ = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
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
                Contact contact;
                switch(state){
                    //电话等待接听
                    case TelephonyManager.CALL_STATE_RINGING:
                        incomingFlag = true;
                        Log.i("PhoneReceiver", "CALL IN RINGING :" + incomingNumber);
                        Boolean block = false;
                        contact = MyApp.contacts_manager_.getContactByPhone(incomingNumber);
                        if (contact == null) {
                            Integer har = MyApp.harassing_call_manager_.isHarassingPhone(
                                    incomingNumber
                            );
                            if (har > 0) {
                                block = true;
                            }
                            if (!block)startInfoWindow("未知号码 " + incomingNumber);
                        }
                        else {
                            block = contact.isBlock();
                            if (!block)startInfoWindow(contact.getName());
                        }
                        if (block) {
                            try {
                                tm_.getClass().getMethod("endCall").invoke(tm_);
                                Log.i("PhoneReceiver", "CALL IN HANG UP :" + incomingNumber);
                                if (contact == null) {
                                    startNotification(incomingNumber);
                                    startToast(incomingNumber);
                                }
                                else {
                                    startNotification(contact.getName());
                                    startToast(contact.getName());
                                }
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
                            Log.i("PhoneReceiver", "CALL OUT START :" + incomingNumber);
                            contact = MyApp.contacts_manager_.getContactByPhone(incomingNumber);
                            if (contact == null) {
                                startInfoWindow(incomingNumber);
                            }
                            else {
                                startInfoWindow(contact.getName());
                            }
                        }
                        break;
                    //电话挂机
                    case TelephonyManager.CALL_STATE_IDLE:
                        stopInfoWindow();
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

        public void startToast(String msg) {
            Toast.makeText(getApplicationContext(),"来电已被拦截,来自:" + msg, Toast.LENGTH_LONG).show();
        }

        public void startNotification(String msg) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_white);
            mBuilder.setContentTitle("来电已被拦截");
            mBuilder.setContentText("来自 " + msg);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }

        public void startInfoWindow(String msg) {
//            if (window_alert_view_ != null) {
//                return;
//            }
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.RGBA_8888;
            window_alert_view_ = new TextView(getApplicationContext());
            window_alert_view_.setText("秘连提示，电话来自: " + msg);
            window_manager_.addView(window_alert_view_, params);
        }

        public void stopInfoWindow() {
            try {
                if (window_alert_view_!=null) {
                    window_manager_.removeView(window_alert_view_);
                }
            }
            catch (Exception e) {

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        shared_preference_ = getApplicationContext().getSharedPreferences("data", MODE_PRIVATE);
        auth_key_ = shared_preference_.getString("auth_key", "");
        auth_id_ = shared_preference_.getString("auth_id", "");

        key_manager_ = SecretKeyManager.getSecretKeyManager(shared_preference_);
        contacts_manager_ = ContactsManager.getContactsManager(getApplicationContext());
        harassing_call_manager_ = HarassingCallManager.getCloudBlackListManager(getApplicationContext());


        IntentFilter receiver_filter_ = new IntentFilter();
        receiver_filter_.addAction("android.intent.action.PHONE_STATE");
        receiver_filter_.addAction("android.intent.action.NEW_OUTGOING_CALL");
        registerReceiver(phone_receiver_, receiver_filter_);
    }

    public static Boolean checkLoginStatus() {
        return !auth_key_.equals("") && !auth_id_.equals("");
    }

    public static Boolean checkKeyStatus() {
        return key_manager_.haveKeys();
    }

    public static void clearLoginStatus() {
        updateLoginStatus("", "");
        key_manager_.clearKeys();
    }
    public static void updateLoginStatus(String auth_id, String auth_key) {
        auth_id_ = auth_id;
        auth_key_ = auth_key;
        SharedPreferences.Editor editor =shared_preference_.edit();
        editor.putString("auth_key", auth_key_);
        editor.putString("auth_id", auth_id_);
        editor.apply();
    }

    public static JSONObject getAuthJson() {
        try {
            return new JSONObject().
                    put("auth_id", auth_id_).
                    put("auth_key", auth_key_);
        }
        catch (Exception e) {
            Log.e("auth", "make auth json error. " + e.getMessage());
            return null;
        }
    }

    public static JSONObject HttpPostJson(String url, JSONObject json) {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            if(response.code() == HttpURLConnection.HTTP_OK) {
                JSONTokener json_tokener = new JSONTokener(response.body().string());
                response.body().close();
                try {
                    return ((JSONObject)json_tokener.nextValue()).
                            put("status_code", response.code());
                }
                catch (JSONException e) {
                    return new JSONObject().put("status_code", response.code());
                }
            }
            else {
                response.body().close();
                return new JSONObject().put("status_code", response.code());
            }
        }
        catch (Exception e) {
            Log.e("http", "http post json failed. " + e.getMessage());
            return null;
        }
    }


}
