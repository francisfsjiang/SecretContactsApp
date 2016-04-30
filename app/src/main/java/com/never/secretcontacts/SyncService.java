package com.never.secretcontacts;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.SystemClock;
import android.util.Log;

import com.never.secretcontacts.util.AlarmReceiver;
import com.never.secretcontacts.util.Contact;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int start_id) {
        Log.i("service", "service on start command");
        Log.i("service", "executed at " + new Date().toString());
        syncProcess();
        startSyncAlarmer();
        return super.onStartCommand(intent, flags, start_id);
    }

    private void startSyncAlarmer() {
        Log.i("service", "starting alarmer.");
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int next_time = 1000 * 10;
        long trigger_time = SystemClock.elapsedRealtime() + next_time;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger_time, pi);
        Log.i("service", "finish starting alarmer.");
    }

    private static final Integer g_sync_task_lock_ = 0;
    private static SyncTask g_sync_task_ = null;
    private void syncProcess() {
        Log.i("service", "sync process start");
        synchronized (g_sync_task_lock_) {
            if (g_sync_task_ != null)return;
            g_sync_task_ = new SyncTask();
        }
        g_sync_task_.execute((Void) null);


        g_sync_task_ = null;
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
            syncProcess();
        }

        public SyncService getService() {
            return SyncService.this;
        }
    }



    public class SyncTask extends AsyncTask<Void, Void, Integer> {

//        private final String mEmail;
//        private final String mPassword;
//
//        SyncTask(String email, String password) {
//            mEmail = email;
//            mPassword = password;
//        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                JSONObject json = MyApp.getAuthJson();
                if (json == null) {
                    return -1;
                }
                JSONObject resp_json = MyApp.HttpPostJson(MyApp.URL_POLLING, json);
                if(resp_json == null) {
                    return -2;
                }
                int status_code = resp_json.getInt("status_code");
                Log.i("http", "code " + status_code);
                if(status_code == HttpURLConnection.HTTP_OK) {
                    processPollingList(resp_json);
                    return 1;
                }
                else if (status_code == HttpURLConnection.HTTP_FORBIDDEN) {
                    return -3;
                }
                else {
                    return -2;
                }
            }
            catch (Exception e) {
                Log.e("network", "Network failed." + e.getMessage());
            }

            return -2;
        }

        private Boolean processPollingList(JSONObject json) {




            return false;
        }

        @Override
        protected void onPostExecute(final Integer res) {
            g_sync_task_ = null;

            Log.i("service", "task finish, res " + res.toString());
            if (res == 1) {

            }
            else if (res == -1){
                Log.i("service", "unknown error. ");
            }
            else if (res == -2){
                Log.i("service", "network error. ");
            }
            else if (res == -3){
                Log.i("service", "auth failed. ");
            }
        }

        @Override
        protected void onCancelled() {
            g_sync_task_ = null;
            synchronized (g_sync_task_lock_) {
                if (g_sync_task_ != null)return;
                g_sync_task_ = new SyncTask();
            }
        }
    }

}
