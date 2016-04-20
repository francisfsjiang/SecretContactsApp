package com.never.secretcontacts;

import android.app.Application;
import android.content.SharedPreferences;

public class MyApplication extends Application{

    private static Boolean login_status;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences shared_preference = getApplicationContext().getSharedPreferences("data", MODE_PRIVATE);
        login_status = shared_preference.getBoolean("login_status", false);


    }

    public static Boolean getLoginStatus() {
        return login_status;
    }


}
