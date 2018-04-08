package com.example.android.securenetworkpinning;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class RequestManager {
    private static final String TAG = RequestManager.class.getSimpleName();


    // Got the pem file using the following command:
    // openssl s_client -showcerts -servername httpbin.org -connect httpbin.org:443 </dev/null
    // Converted the pem to a crt using the following command:
    // openssl x509 -outform der -in httpbin.pem -out httpbin.crt
    private static final String HTTP_BIN_CRT_FILE = "httpbin.crt";

    // Downloaded from https://pki.google.com/
    private static final String NEWS_GOOGLE_CRT_FILE = "GIAG2.crt";

    private static RequestManager sInstance;

    public static synchronized RequestManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RequestManager(context.getApplicationContext());
        }

        return sInstance;
    }

    private HashMap<String, KeyStore> mTrustStores = new HashMap<>();

    private RequestManager(Context context) {
        try {
            loadTrustStore(context, "https://httpbin.org", HTTP_BIN_CRT_FILE);
            loadTrustStore(context, "https://news.google.com", NEWS_GOOGLE_CRT_FILE);
        } catch (Exception e) {
            Log.e("RequestManager", "Unable to load trust store", e);
        }
    }

    private void loadTrustStore(Context context, String domain, String storeFile) throws IOException,
            KeyStoreException, CertificateException, NoSuchAlgorithmException, URISyntaxException {
        AssetManager assetManager = context.getAssets();
        InputStream input = assetManager.open(storeFile);

        //Create a certificate from the .pem file
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate cert;
        String subject;
        try {
            cert = factory.generateCertificate(input);
            subject = ((X509Certificate) cert).getSubjectDN().toString();
            Log.d(TAG, "Certificate read: " + subject);
        } finally {
            input.close();
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null); //This creates an empty store
        keyStore.setCertificateEntry(subject, cert);

        mTrustStores.put(getDomainName(domain), keyStore);
    }

    public String makeRequest(String endpoint) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        return parseResponse(urlConnection);
    }

    public String makeSecureRequest(String endpoint) throws NoSuchAlgorithmException, IOException,
            KeyManagementException, URISyntaxException, MissingPublicCertificateFile,
            KeyStoreException {
        String domain = getDomainName(endpoint);
        if (!mTrustStores.containsKey(domain)) {
            throw new MissingPublicCertificateFile(domain);
        }

        KeyStore keyStore = mTrustStores.get(domain);

        URL url = new URL(endpoint);
        if (!url.getProtocol().equals("https")) {
            throw new IllegalArgumentException("You must use an https URL!");
        }

        // Initialize TrustManager from our pinned keystore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        // Set SSLSocketFactory to validate against our TrustManager
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

    private String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public class MissingPublicCertificateFile extends Exception {
        private MissingPublicCertificateFile(String url) {
            super("Missing crt file for " + url);
        }
    }
}
