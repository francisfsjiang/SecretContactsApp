package com.never.secretcontacts.util;

import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.Key;

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

    public void clearKeys() {
        saveKeyPair("", "");
    }

    public void saveKeyPair(String pri_key, String pub_key) {
        pri_key_ = pri_key;
        pub_key_ = pub_key;
        SharedPreferences.Editor editor = shared_preferences_.edit();
        editor.putString("pri_key", pri_key_);
        editor.putString("pub_key", pub_key_);
        editor.apply();
    }

    public class EncryptResult {
        public String aes_key;
        public String content;
        EncryptResult(String aes_key, String content) {
            this.aes_key = aes_key;
            this.content = content;
        }
    }

    public EncryptResult encrypt (String content) throws Exception{
        Key aes_key = CipherAESandRSA.generateAESKey();
        byte[] cipher_text_byte = CipherAESandRSA.aesEncrypt(content.getBytes(Charset.forName("UTF-8")), aes_key);
        String cipher_text_str = Base64.encodeToString(cipher_text_byte, Base64.DEFAULT);
        byte[] encrypted_aes_key_byte = CipherAESandRSA.wrapAESKey(
                aes_key,
                CipherAESandRSA.getPublicKeyFromString(pub_key_)
        );
        String encrypted_ase_key_str = Base64.encodeToString(encrypted_aes_key_byte, Base64.DEFAULT);

        return new EncryptResult(encrypted_ase_key_str, cipher_text_str);
    }

    public String decrypt(String encrypted_aes_key_str, String encrypted_content) throws Exception{
        Key o_aes_key = CipherAESandRSA.unwrapAESKey(
                Base64.decode(encrypted_aes_key_str, Base64.DEFAULT),
                CipherAESandRSA.getPrivateKeyFromString(pri_key_)
        );
        byte[] raw_text_byte = CipherAESandRSA.aesDecrypt(
                Base64.decode(encrypted_content, Base64.DEFAULT),
                o_aes_key
        );
        return new String(raw_text_byte, Charset.forName("UTF-8"));
    }

}
