package com.example.android.securenetworkpinning;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class RequestManager {

    private static final String STORE_FILE = "httpbin.store";
    private static final String STORE_PASS = "newcircle";

    private static RequestManager sInstance;

    public static synchronized RequestManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RequestManager(context.getApplicationContext());
        }

        return sInstance;
    }

    private KeyStore mTrustStore;

    private RequestManager(Context context) {
        try {
            loadTrustStore(context);
        } catch (Exception e) {
            Log.w("RequestManager", "Unable to load trust store", e);
        }
    }

    private void loadTrustStore(Context context) throws
            IOException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException {
        AssetManager assetManager = context.getAssets();
        InputStream input = assetManager.open(STORE_FILE);
        mTrustStore = KeyStore.getInstance("BKS");

        mTrustStore.load(input, STORE_PASS.toCharArray());
    }

    public String makeRequest(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

        return parseResponse(urlConnection);
    }

    public String makeSecureRequest(String endpoint) throws
            CertificateException, NoSuchAlgorithmException,
            IOException, KeyManagementException, KeyStoreException {
        URL url = new URL(endpoint);
        if (!url.getProtocol().equals("https")) {
            throw new IllegalArgumentException("You must use an https URL!");
        }

        //Initialize TrustManager from our pinned keystore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(mTrustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
        //Set SSLSocketFactory to validate against our TrustManager
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());

        return parseResponse(urlConnection);
    }

    private String parseResponse(URLConnection connection) throws IOException {
        InputStream in = connection.getInputStream();
        String encoding = connection.getContentEncoding();
        int contentLength = connection.getContentLength();
        if (encoding == null) {
            encoding = "UTF-8";
        }

        byte[] buffer = new byte[16384];

        int length = contentLength > 0 ? contentLength : 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(length);

        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        return new String(out.toByteArray(), encoding);
    }
}
