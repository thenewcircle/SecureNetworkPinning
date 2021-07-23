package com.example.android.securenetworkpinning

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.android.securenetworkpinning.databinding.ActivityMainBinding
import kotlinx.coroutines.*

class MainActivity : Activity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
    }

    fun onRequestClick(v: View) {
        when (activityMainBinding.options.checkedRadioButtonId) {
            R.id.option_google -> requestTask("news.google.com/rss")
            R.id.option_httpbin -> requestTask("httpbin.org/xml")
            else -> throw IllegalArgumentException("Invalid Option Selected")
        }
        setResultText(null)
    }

    private fun setResultText(text: CharSequence?) {
        activityMainBinding.textResults.text = text
    }

    @DelicateCoroutinesApi
    private fun requestTask(url: String) {
        val useTls = activityMainBinding.secureEnable.isChecked
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result =
                        if (useTls) RequestManager.makeSecureRequest("https://$url")
                        else RequestManager.makeRequest("http://$url")

                withContext(Dispatchers.Main) {
                    setResultText(result)
                }
            } catch (e: Exception) {
                Log.w("RequestTask", "Unable to make request", e)
            }
        }
    }
}
