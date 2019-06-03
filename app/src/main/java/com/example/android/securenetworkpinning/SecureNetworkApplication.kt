package com.example.android.securenetworkpinning

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SecureNetworkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}