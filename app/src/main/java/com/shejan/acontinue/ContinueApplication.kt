package com.shejan.acontinue

import android.app.Application
import com.shejan.acontinue.core.di.AppContainer

class ContinueApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
