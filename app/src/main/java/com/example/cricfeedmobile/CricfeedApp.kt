package com.example.cricfeedmobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CricfeedApp : Application(){
    override fun onCreate() {
        super.onCreate()
    }
}