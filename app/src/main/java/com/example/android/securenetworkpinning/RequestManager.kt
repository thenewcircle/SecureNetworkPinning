package com.example.android.securenetworkpinning


import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset

object RequestManager {

    @Throws(IOException::class)
    fun makeRequest(endpoint: String): String {
        val url = URL(endpoint)
        val urlConnection = url.openConnection() as HttpURLConnection

        return parseResponse(urlConnection)
    }

    @Throws(IOException::class)
    private fun parseResponse(connection: URLConnection): String {
        val input = connection.getInputStream()
        val encoding = connection.contentEncoding ?: "UTF-8"
        val contentLength = connection.contentLength

        val buffer = ByteArray(16384)

        val length = if (contentLength > 0) contentLength else 0
        val out = ByteArrayOutputStream(length)

        var read: Int = input.read(buffer)
        while (read != -1) {
            out.write(buffer, 0, read)
            read = input.read(buffer)
        }

        return String(out.toByteArray(), Charset.forName(encoding))
    }
}
