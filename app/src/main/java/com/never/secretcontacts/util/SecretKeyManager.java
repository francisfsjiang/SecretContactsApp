package com.never.secretcontacts.util;

import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class SecretKeyManager {

    SharedPreferences shared_preferences_;

    private String pri_key_;
    private String pub_key_;

    private final int MAX_ENCRYPT_BLOCK = 512;
    private final int MAX_DECRYPT_BLOCK = 512;

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


    private RSAPrivateKey getPrivateKeyFromString(String key) throws IOException, GeneralSecurityException {
        byte[] encoded = Base64.decode(key, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
    }

    private RSAPublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
        byte[] encoded = Base64.decode(key, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) kf.generatePublic(keySpec);
    }


    public String encryptByPublicKey(String text)
            throws Exception {
        RSAPublicKey publicKey = getPublicKeyFromString(pub_key_);
        byte[] data = text.getBytes();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }


    public String decryptByPrivateKey(String data)
            throws Exception {
        byte[] encryptedData = Base64.decode(data, Base64.DEFAULT);
        RSAPrivateKey privateKey = getPrivateKeyFromString(pri_key_);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData);
    }

}
