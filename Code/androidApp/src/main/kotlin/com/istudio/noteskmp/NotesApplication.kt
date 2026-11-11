package com.istudio.noteskmp

import android.app.Application
import com.istudio.noteskmp.di.initKoin
import org.koin.android.ext.koin.androidContext

class NotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@NotesApplication)
        }
    }
}
