package com.never.secretcontacts.util;

import android.content.SharedPreferences;

public class SecretKeyManager {

    SharedPreferences shared_preferences_;

    private String pri_key_;
    private String pub_key_;

    private SecretKeyManager(SharedPreferences shared_preferences) {
        shared_preferences_ = shared_preferences;
        pri_key_ = shared_preferences_.getString("pri_key", "");
        pub_key_ = shared_preferences_.getString("pub_key", "");
    }

    private static SecretKeyManager secret_key_manager_ = null;

    public static SecretKeyManager getSecretKeyManager(SharedPreferences shared_preferences) {
        if (secret_key_manager_ == null) {
            secret_key_manager_ = new SecretKeyManager(shared_preferences);
        }
        return secret_key_manager_;
    }

    public Boolean haveKeys() {
        return !pri_key_.equals("") && !pub_key_.equals("");
    }

    public void saveKeyPair(String pri_key, String pub_key) {
        pri_key_ = pri_key;
        pub_key_ = pub_key;
        SharedPreferences.Editor editor = shared_preferences_.edit();
        editor.putString("pri_key", pri_key_);
        editor.putString("pub_key", pub_key_);
        editor.apply();
    }

}
