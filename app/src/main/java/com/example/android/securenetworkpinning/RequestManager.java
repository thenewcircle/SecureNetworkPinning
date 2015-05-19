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

    private static RequestManager sInstance;

    public static synchronized RequestManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RequestManager(context.getApplicationContext());
        }

        return sInstance;
    }

    private RequestManager(Context context) {
        //TODO: Create and load the local trust store
    }

    public String makeRequest(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

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
