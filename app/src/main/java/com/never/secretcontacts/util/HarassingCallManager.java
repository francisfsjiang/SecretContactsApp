package com.never.secretcontacts.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class HarassingCallManager {

    private DBHelper db_helper_;

    private SQLiteDatabase db_;

    SharedPreferences shared_preferences_;

    private String CLOUD_TABLE_NAME = "cloud_harassing";
    private String MY_TABLE_NAME = "my_harassing";

    private HarassingCallManager(Context context) {
        shared_preferences_ = context.getSharedPreferences("harassing", Context.MODE_PRIVATE);
        db_helper_ = new DBHelper(context, "SecretContactsBlock.db", 1);
        db_ = db_helper_.getWritableDatabase();

    }

    private static HarassingCallManager cloud_blacklist_manager_ = null;

    public static HarassingCallManager getCloudBlackListManager(Context context) {
        if (cloud_blacklist_manager_ == null) {
            cloud_blacklist_manager_ = new HarassingCallManager(context);
        }
        return cloud_blacklist_manager_;
    }

    public Integer isHarassingPhone(String phone_number) {
        Cursor cursor = db_.query(CLOUD_TABLE_NAME, new String[]{"mark_time"}, "phone = ?", new String[]{phone_number},null, null, null);
        if (cursor.moveToFirst()) {
            Integer ret = cursor.getInt(cursor.getColumnIndex("mark_time"));
            cursor.close();
            return ret;
        }
        else {
            return -1;
        }
    }

    public void setCloudHarassingUpdateTime(Integer time_stamp) {
        SharedPreferences.Editor editor = shared_preferences_.edit();
        editor.putInt("harassing_update_time", time_stamp);
        editor.apply();
    }

    public Integer getCloudHarassingUpdateTime() {
        return shared_preferences_.getInt("harassing_update_time", 0);
    }

    public void updateCloudHarassing(JSONArray json_arr, Integer update_time) {
        List<String> phone_list = new ArrayList<>();
        List<Integer> mark_time_list = new ArrayList<>();
        try {
            JSONArray temp_arr;
            for (int i = 0; i < json_arr.length(); ++i) {
                temp_arr = json_arr.getJSONArray(i);
                phone_list.add(temp_arr.getString(0));
                mark_time_list.add(temp_arr.getInt(1));
            }
        }
        catch (Exception e) {
            return;
        }
        if (phone_list.size() != mark_time_list.size() || phone_list.size() == 0) {
            return;
        }
        db_.delete(CLOUD_TABLE_NAME, null, null);
        ContentValues values;
        int count = 0;
        for (int i = 0; i < phone_list.size(); ++i) {
            values = new ContentValues();
            values.put("phone", phone_list.get(i));
            values.put("mark_time", mark_time_list.get(i));
            Log.i("harassing", values.toString());
            count += db_.insert(CLOUD_TABLE_NAME, null, values);
        }
        setCloudHarassingUpdateTime(update_time);
        Log.i("harassing", "update " + phone_list.size() + "records");
    }

    public List<String> getMyHarassing() {
        List<String> arr = new ArrayList<>();
        Cursor cursor = db_.query(MY_TABLE_NAME, new String[]{"phone"}, "new = 1", null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                arr.add(cursor.getString(cursor.getColumnIndex("content")));
            }while (cursor.moveToNext());
            cursor.close();
            ContentValues values = new ContentValues();
            values.put("new", 0);
            db_.update(MY_TABLE_NAME, values, "new = 1", null);
            return arr;
        }
        else {
            return arr;
        }
    }

    public void addToMyHarassing(String phone_number) {
        Cursor cursor = db_.query(MY_TABLE_NAME, new String[]{"phone"}, "phone = ?", new String[]{phone_number},null, null, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            return;
        }
        ContentValues value = new ContentValues();
        value.put("phone", phone_number);
        value.put("new", 1);
        db_.insert(MY_TABLE_NAME , null, value);
    }

    public void deleteAllData() {
        db_.delete(CLOUD_TABLE_NAME, null, null);
        db_.delete(MY_TABLE_NAME, null, null);
    }

    private class DBHelper extends SQLiteOpenHelper {

        private static final String CREATE_CLOUD_HARASSING_TABLE =
                "create table cloud_harassing (" +
                        "phone text primary key," +
                        "mark_time integer)";

        private static final String CREATE_MY_HARASSING_TABLE =
                "create table my_harassing (" +
                        "phone text primary key," +
                        "new integer)";

        private Context context_;

        public DBHelper(Context context,
                        String name,
                        int version) {
            super(context, name, null, version);
            context_ = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CLOUD_HARASSING_TABLE);
            db.execSQL(CREATE_MY_HARASSING_TABLE);
            Log.i("db", "table created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
            Log.i("db", "table upgrade");
        }
    }
}
