package com.example.android.securenetworkpinning

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView

class MainActivity : Activity() {
    private var textView: TextView? = null
    private var endpointOptions: RadioGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.text_results)
        endpointOptions = findViewById(R.id.options)
    }

    fun onRequestClick(v: View) {
        val task = RequestTask()
        when (endpointOptions!!.checkedRadioButtonId) {
            R.id.option_google -> task.execute("news.google.com/rss")
            R.id.option_httpbin -> task.execute("httpbin.org/xml")
            else -> throw IllegalArgumentException("Invalid Option Selected")
        }
        setResultText(null)
    }

    fun setResultText(text: CharSequence?) {
        textView!!.text = text
    }

    private inner class RequestTask : AsyncTask<String, Int, String>() {

        override fun doInBackground(vararg params: String): String {
            var url = params[0]

            return try {
                url = "http://$url"
                RequestManager.makeRequest(url)
            } catch (e: Exception) {
                Log.w("RequestTask", "Unable to make request", e)
                "Error"
            }
        }

        override fun onPostExecute(result: String) {
            setResultText(result)
        }
    }
}