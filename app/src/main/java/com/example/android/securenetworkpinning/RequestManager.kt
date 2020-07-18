package com.example.android.securenetworkpinning

import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.HashMap

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.math.max

object RequestManager {
    private val TAG = RequestManager::class.java.simpleName

    // Got the pem file using the following command:
    // openssl s_client -showcerts -servername httpbin.org -connect httpbin.org:443 </dev/null
    private const val HTTP_BIN_PEM_FILE = "httpbin.pem"

    // Converted the pem to a crt using the following command:
    // openssl x509 -outform der -in httpbin.pem -out httpbin.crt
    private const val HTTP_BIN_CRT_FILE = "httpbin.crt"

    // Downloaded from https://pki.google.com/
    private const val NEWS_GOOGLE_CRT_FILE = "GSR2.crt"

    private val trustStores = HashMap<String, KeyStore>()

    init {
        try {
            loadTrustStore("https://httpbin.org", HTTP_BIN_PEM_FILE) // OR CRT FILE
            loadTrustStore("https://news.google.com", NEWS_GOOGLE_CRT_FILE)
        } catch (e: Exception) {
            Log.e("RequestManager", "Unable to load trust store", e)
        }

    }

    @Throws(IOException::class, KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, URISyntaxException::class)
    private fun loadTrustStore(domain: String, certAsset: String) {
        // Our Context is used to access the AssetManager which provides
        // an InputStream to the .pem/.crt file
        val inputStream = SecureNetworkApplication.context.assets.open(certAsset)

        // Create a certificate from the .pem/.crt file
        // We need a CertificateFactory with the X.509 type
        val factory = CertificateFactory.getInstance("X.509")
        var cert: Certificate? = null
        val subject: String

        inputStream.use { input ->
            // Generate a certificate object
            cert = factory.generateCertificate(input)
        }

        // Extract the Common Name.
        // Run `logcat |grep 'Certificate read'` to see the value
        subject = (cert as X509Certificate).subjectDN.toString()
        Log.d(TAG, "Certificate read: $subject")

        // Create a KeyStore object
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null) //This creates an empty store
        keyStore.setCertificateEntry(subject, cert)

        // Store the KeyStore object in our HashMap
        trustStores[getDomainName(domain)] = keyStore
    }

    // A helper method to extract a domain name from a URL
    @Throws(URISyntaxException::class)
    private fun getDomainName(url: String): String {
        val uri = URI(url)
        val domain = uri.host
        return if (domain.startsWith("www.")) domain.substring(4) else domain
    }

    @Throws(IOException::class)
    fun makeRequest(endpoint: String): String {
        val url = URL(endpoint)
        val urlConnection = url.openConnection() as HttpURLConnection
        return parseResponse(urlConnection)
    }

    @Throws(NoSuchAlgorithmException::class, IOException::class, KeyManagementException::class, URISyntaxException::class, MissingPublicCertificateFile::class, KeyStoreException::class)
    fun makeSecureRequest(endpoint: String): String {
        val domain = getDomainName(endpoint)
        if (!trustStores.containsKey(domain)) {
            throw MissingPublicCertificateFile(domain)
        }

        val keyStore = trustStores[domain]

        val url = URL(endpoint)
        require(url.protocol == "https") { "You must use an https URL!" }

        // Initialize TrustManager from our pinned keystore
        val factory = TrustManagerFactory.getInstance("X509")
        factory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, factory.trustManagers, null)

        val urlConnection = url.openConnection() as HttpsURLConnection
        // Set SSLSocketFactory to validate against our TrustManager
        urlConnection.sslSocketFactory = sslContext.socketFactory

        return parseResponse(urlConnection)
    }

    @Throws(IOException::class)
    private fun parseResponse(connection: URLConnection): String {
        val input = connection.getInputStream()
        val encoding = connection.contentEncoding ?: "UTF-8"
        val contentLength = connection.contentLength

        val buffer = ByteArray(16384)
        val length = max(contentLength, 0)
        val out = ByteArrayOutputStream(length)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        return String(out.toByteArray(), Charset.forName(encoding))
    }

    class MissingPublicCertificateFile(url: String) : Exception("Missing crt file for $url")
}
