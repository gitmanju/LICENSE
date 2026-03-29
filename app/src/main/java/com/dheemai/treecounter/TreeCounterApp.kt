package com.dheemai.treecounter

import android.app.Application
import org.osmdroid.config.Configuration

class TreeCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
    }
}
