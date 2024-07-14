package com.mioshek.podcastreader

import android.app.Application

class LyricsApp: Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}