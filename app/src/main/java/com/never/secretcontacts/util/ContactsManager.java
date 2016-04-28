package com.never.secretcontacts.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ContactsManager {

    private enum OP {
        NEW(0), UPDATED(1), DELETED(2);
        int value_;
        OP(int value) {
            value_ = value;
        }
        public int getValue_() {
            return value_;
        }
    }

    private DBHelper db_helper_;

    private SQLiteDatabase db_;

    private ContactsManager(Context context) {
        db_helper_ = new DBHelper(context, "secret_contacts", 1);
        db_ = db_helper_.getWritableDatabase();
    }

    private static ContactsManager contacts_manager_ = null;

    public static ContactsManager getContactsManager(Context context) {
        if (contacts_manager_ == null) {
            contacts_manager_ = new ContactsManager(context);
        }
        return contacts_manager_;
    }



    private class DBHelper extends SQLiteOpenHelper {

        private static final String CREATE_CONTACTS_TABLE =
                "create table contacts(" +
                        "id text primary key," +
                        "content text," +
                        "last_op integer," +
                        "last_op_time integer)";

        private Context context_;

        public DBHelper(Context context,
                        String name,
                        int version) {
            super(context, name, null, version);
            context_ = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CONTACTS_TABLE);
            Log.i("db", "table created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
            Log.i("db", "table upgrade");
        }
    }
}
