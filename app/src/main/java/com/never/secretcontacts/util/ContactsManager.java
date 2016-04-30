package com.never.secretcontacts.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContactsManager {

    private enum OP {
        None(0), NEW(1), UPDATE(2), DELETE(3);
        int value;
        OP(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    private CharacterParser character_parser_;

    private DBHelper db_helper_;

    private SQLiteDatabase db_;

    private String TABLE_NAME = "secret_contacts";

    private ContactsManager(Context context) {
        db_helper_ = new DBHelper(context, "SecretContacts.db", 1);
        db_ = db_helper_.getWritableDatabase();

        character_parser_ = CharacterParser.getInstance();
    }

    private static ContactsManager contacts_manager_ = null;

    public static ContactsManager getContactsManager(Context context) {
        if (contacts_manager_ == null) {
            contacts_manager_ = new ContactsManager(context);
        }
        return contacts_manager_;
    }

    public List<Contact> getAllContacts() {

        List<Contact> mSortList = new ArrayList<Contact>();

        Cursor cursor = db_.query(TABLE_NAME, new String[]{"content"}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Contact sortModel = Contact.loadContactFromJsonString(
                        cursor.getString(cursor.getColumnIndex("content"))
                );
                if (sortModel == null) {
                    continue;
                }
                //汉字转换成拼音
                String pinyin = character_parser_.getSelling(sortModel.getName());
                String sortString = pinyin.substring(0, 1).toUpperCase();

                // 正则表达式，判断首字母是否是英文字母
                if(sortString.matches("[A-Z]")){
                    sortModel.setSortLetters(sortString.toUpperCase());
                }else{
                    sortModel.setSortLetters("#");
                }

                mSortList.add(sortModel);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return mSortList;
    }

    public Contact getContact(String contact_id) {
        Cursor cursor = db_.query(TABLE_NAME, new String[]{"content"}, "id = ?", new String[]{contact_id},null, null, null);
        if (cursor.moveToFirst()) {
            Contact contact = Contact.loadContactFromJsonString(
                    cursor.getString(cursor.getColumnIndex("content"))
            );
            cursor.close();
            return contact;
        }
        else {
            return null;
        }
    }

    public void createContact(Contact contact) {
        contact.setId(UUID.randomUUID().toString());
        ContentValues value = new ContentValues();
        value.put("id", contact.getId());
        value.put("content", Contact.dumpContactToJsonString(contact));
        value.put("last_op", OP.NEW.getValue());
        value.put("last_op_time", System.currentTimeMillis()/1000);
        db_.insert(TABLE_NAME, null, value);
        Log.i("contact", "new contact created.");
        Log.i("contact", "name: " + contact.getName());
        Log.i("contact", "id: " + contact.getId());
    }

    public void updateContact(Contact contact) {
        ContentValues value = new ContentValues();
        value.put("content", Contact.dumpContactToJsonString(contact));
        value.put("last_op", OP.UPDATE.getValue());
        value.put("last_op_time", System.currentTimeMillis() / 1000);
        db_.update(TABLE_NAME, value, "id = ?", new String[]{contact.getId()});
    }

    public void deleteContact(Contact contact) {
        ContentValues value = new ContentValues();
        value.put("last_op", OP.DELETE.getValue());
        value.put("last_op_time", System.currentTimeMillis()/1000);
        db_.delete(TABLE_NAME, "id = ?", new String[]{contact.getId()});
    }

    public void clearContactOP(Contact contact) {
        ContentValues value = new ContentValues();
        value.put("last_op", OP.None.getValue());
        value.put("last_op_time", 0);
        db_.update(TABLE_NAME, value, "id = ?", new String[]{contact.getId()});
    }

    private class DBHelper extends SQLiteOpenHelper {

        private static final String CREATE_CONTACTS_TABLE =
                "create table secret_contacts (" +
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
