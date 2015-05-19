package com.example.android.securenetworkpinning;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.X509TrustManager;

public class PinnedTrustManager implements X509TrustManager {

    private List<String> mPins = new ArrayList<String>();

    public PinnedTrustManager(String... initialPins) {
        for (String pin : initialPins) {
            addPin(pin);
        }
    }

    public void addPin(String pin) {
        if (pin == null) {
            throw new IllegalArgumentException("Pins cannot be null");
        }
        mPins.add(pin);
    }

    public void clearPins() {
        mPins.clear();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        throw new CertificateException("Client certificates not supported");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        for (X509Certificate cert : chain) {
            if (isValidPin(cert)) {
                //Found a matching certificate in the chain
                return;
            }
        }

        throw new CertificateException("No pinned certificates found");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    private boolean isValidPin(X509Certificate certificate)
            throws CertificateException {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA1");
            final byte[] pulicKey = certificate.getPublicKey().getEncoded();
            final byte[] rawPin = digest.digest(pulicKey);
            final String pin = Base64.encodeToString(rawPin, Base64.NO_WRAP);

            for (String validPin : mPins) {
                if (validPin.equals(pin)) {
                    return true;
                }
            }

            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(e);
        }
    }
}
