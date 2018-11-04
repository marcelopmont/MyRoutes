package com.mpm.myroutes

import android.app.Application
import com.facebook.stetho.Stetho

class MyRouteApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }

}