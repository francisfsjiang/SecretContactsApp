package com.never.secretcontacts;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;


public class MyApplication extends Application{

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

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

    public static JSONObject HttpPostJson(String url, JSONObject json, Integer status_code) {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            status_code = response.code();
            if(status_code == HttpURLConnection.HTTP_OK) {
                JSONTokener json_tokener = new JSONTokener(response.body().string());
                return (JSONObject)json_tokener.nextValue();
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            Log.e("http", "http post json failed. code " + status_code + ". " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


}
