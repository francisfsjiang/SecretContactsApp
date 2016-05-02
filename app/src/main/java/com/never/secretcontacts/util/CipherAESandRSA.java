package com.never.secretcontacts.util;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class CipherAESandRSA {
    public static RSAPrivateKey getPrivateKeyFromString(String key) throws Exception {
        byte[] encoded = Base64.decodeBase64(key);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
    }

    public static RSAPublicKey getPublicKeyFromString(String key) throws Exception {
        byte[] encoded = Base64.decodeBase64(key);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) kf.generatePublic(keySpec);
    }

    public static Key generateAESKey() throws Exception{
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecureRandom random = new SecureRandom();
        keygen.init(random);
        return keygen.generateKey();
    }

    public static byte[] aesEncrypt(byte[] raw_text, Key aes_key) throws Exception{
        return crypt(raw_text, Cipher.ENCRYPT_MODE, aes_key);
    }

    public static byte[] aesDecrypt(byte[] cipher_text, Key aes_key_) throws Exception{
        return crypt(cipher_text, Cipher.DECRYPT_MODE, aes_key_);
    }

    public static byte[] crypt(byte[] data, int mode, Key aes_key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, aes_key);

        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int inputSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(inputSize);
        byte[] inBytes = new byte[inputSize];
        byte[] outBytes = new byte[outputSize];

        int inLength = 0;
        boolean more = true;
        while (more) {
            inLength = in.read(inBytes);
            if (inLength == inputSize) {
                int outLength = cipher.update(inBytes, 0, inputSize, outBytes);
                out.write(outBytes, 0, outLength);
            } else {
                more = false;
            }
        }
        if (inLength > 0)
            outBytes = cipher.doFinal(inBytes, 0, inLength);
        else
            outBytes = cipher.doFinal();
        out.write(outBytes);
        out.flush();
        return out.toByteArray();
    }

    public static byte[] wrapAESKey(Key aes_key, RSAPublicKey public_key) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, public_key);
        return cipher.wrap(aes_key);
    }

    public static Key unwrapAESKey(byte[] wrapedKeyBytes, RSAPrivateKey private_key) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.UNWRAP_MODE, private_key);
        return cipher.unwrap(wrapedKeyBytes, "AES", Cipher.SECRET_KEY);
    }
}
