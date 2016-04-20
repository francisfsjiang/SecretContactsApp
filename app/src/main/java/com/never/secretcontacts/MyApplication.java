package com.never.secretcontacts;

import android.app.Application;
import android.content.SharedPreferences;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;

public class MyApplication extends Application{

    public static final MediaType TEXT
            = MediaType.parse("text/plain; charset=utf-8");

    public static String URL_SITE = "https://sc.404notfound.top/";

    public static String URL_LOGIN = URL_SITE + "api/login";

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

    public static Response HttpPost(String url, RequestBody request_body) throws java.io.IOException{
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(request_body)
                .build();
        return client.newCall(request).execute();
    }


}
