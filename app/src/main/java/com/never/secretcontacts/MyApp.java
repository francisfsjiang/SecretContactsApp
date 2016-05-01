package com.never.secretcontacts;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.never.secretcontacts.util.ContactsManager;
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

    private static String auth_id_;
    private static String auth_key_;

    private static SharedPreferences shared_preference_;

    public static ContactsManager contacts_manager_;

    public static SecretKeyManager key_manager_;

    @Override
    public void onCreate() {
        super.onCreate();
        shared_preference_ = getApplicationContext().getSharedPreferences("data", MODE_PRIVATE);
        auth_key_ = shared_preference_.getString("auth_key", "");
        auth_id_ = shared_preference_.getString("auth_id", "");

        key_manager_ = SecretKeyManager.getSecretKeyManager(shared_preference_);
        contacts_manager_ = ContactsManager.getContactsManager(getApplicationContext());

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
