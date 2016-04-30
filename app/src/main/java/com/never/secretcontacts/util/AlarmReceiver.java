package com.never.secretcontacts.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.never.secretcontacts.SyncService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("service", "on receive.");
        Intent i = new Intent(context, SyncService.class);
        context.startService(i);
    }
}
