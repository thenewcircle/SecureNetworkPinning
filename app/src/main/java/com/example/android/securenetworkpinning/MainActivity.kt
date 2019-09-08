package com.example.android.securenetworkpinning

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onRequestClick(v: View) {
        when (options.checkedRadioButtonId) {
            R.id.option_google -> requestTask("news.google.com/rss")
            R.id.option_httpbin -> requestTask("httpbin.org/xml")
            else -> throw IllegalArgumentException("Invalid Option Selected")
        }
        setResultText(null)
    }

    private fun setResultText(text: CharSequence?) {
        textResults.text = text
    }

    private fun requestTask(url: String) {
        GlobalScope.async {
            try {
                val result = RequestManager.makeRequest("http://$url")
                withContext(Dispatchers.Main) {
                    setResultText(result)
                }
            } catch (e: Exception) {
                Log.w("RequestTask", "Unable to make request", e)
                "Error"
            }
        }
    }
}
