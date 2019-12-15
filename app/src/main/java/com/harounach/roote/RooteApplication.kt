package com.harounach.roote

import android.app.Application
import timber.log.Timber

class RooteApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Setup Timber
        Timber.plant(Timber.DebugTree())
    }
}