package com.mioshek.podcastreader

import android.content.Context

interface AppContainer {
    val lyricsRepository: LyricsRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     */
    override val lyricsRepository: LyricsRepository by lazy {
        LyricsRepository(AppDatabase.getDatabase(context).lyricsDao)
    }
}