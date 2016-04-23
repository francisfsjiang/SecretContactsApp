package com.never.secretcontacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.never.secretcontacts.util.Contact;

public class ContactsManager {

    private DBHelper db_helper_;

    public ContactsManager(Context context) {
        db_helper_ = new DBHelper(context, "secret_contacts", null, 1);

    }

    private class DBHelper extends SQLiteOpenHelper {

        private static final String CREATE_CONTACTS_TABLE =
                "create table contacts(" +
                        "id text primary key autoincrement" +
                        "data text" +
                        "last_op integer" +
                        "last_op_time integer)";

        private Context context_;

        public DBHelper(Context context,
                        String name,
                        SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
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
