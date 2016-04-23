package com.never.secretcontacts;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

import org.json.JSONObject;

public class SyncService extends Service{

    private Binder sync_binder_ = new SyncBinder();

    @Override
    public void onCreate() {
        Log.i("service", "service create");
    }

    @Override
    public Binder onBind(Intent intent) {
        return sync_binder_;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("service", "service unbind");

        return false;
    }

    public void startSyncThread() {

    }

    @Override
    public void onRebind(Intent intent) {
        Log.i("service", "service rebind");
    }

    @Override
    public void onDestroy() {
        Log.i("service", "service destory");
    }

    class SyncBinder extends Binder {
        public void syncNow() {
            Log.i("service", "sync now");
            startSyncThread();
        }
    }

    class SyncThread extends Thread {
        @Override
        public void run() {
            Log.i("service thread", "loop start");
            JSONObject auth_json = MyApp.getAuthJson();

            while (true) {
                try {
                    Log.i("service thread", "in loop");
                    Thread.sleep(60);
                    JSONObject resp_json = MyApp.HttpPostJson(
                            MyApp.URL_POLLING,
                            auth_json
                            );
                    if(resp_json == null || resp_json.getInt("status_code") != 200) {
                        Log.w("service thread", "get json error. ");
                    }


                }
                catch (Exception e) {
                    Log.e("service thread", "error " + e.getMessage());
                }

            }
        }
    }
}
