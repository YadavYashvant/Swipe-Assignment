package com.yashvant.swipe_assignment

import android.app.Application
import com.yashvant.swipe_assignment.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class SwipeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@SwipeApplication)
            modules(appModule)
        }

    }
}

