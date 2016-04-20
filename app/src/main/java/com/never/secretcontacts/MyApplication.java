package com.never.secretcontacts;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MyApplication extends Application{

    final String cert_file_name = "site_cert";

    private static Boolean login_status;

    private static SSLContext ssl_context;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences shared_preference = getApplicationContext().getSharedPreferences("data", MODE_PRIVATE);
        login_status = shared_preference.getBoolean("login_status", false);

        try {
            CertificateFactory cf;
            cf = CertificateFactory.getInstance("X.509");

            Certificate ca;
            InputStream caInput = new BufferedInputStream(new FileInputStream(cert_file_name));
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            caInput.close();

// Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
            ssl_context = SSLContext.getInstance("TLS");
            ssl_context.init(null, tmf.getTrustManagers(), null);
        }
        catch (java.io.FileNotFoundException e) {
            Log.e("cert", "File not found." + e.getMessage());
        }
        catch (java.io.IOException e) {
            Log.e("cert", "io exception." + e.getMessage());
        }
        catch (java.security.cert.CertificateException e) {
            Log.e("cert", "Cert err." + e.getMessage());
        }
        catch (java.security.KeyStoreException e) {
            Log.e("cert", "Keystore." + e.getMessage());
        }
        catch (java.security.NoSuchAlgorithmException e) {
            Log.e("cert", "No algo." + e.getMessage());
        }
        catch (java.security.KeyManagementException e) {
            Log.e("cert", "Key manage err." + e.getMessage());
        }

    }

    public static Boolean getLoginStatus() {
        return login_status;
    }

    public static CloseableHttpClient getHttpClient() {
        return HttpClients.custom().setSSLContext(ssl_context).build();
    }

}
