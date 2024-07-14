package com.mioshek.podcastreader

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            LyricsViewModel(
                theUltimateClockApplication().container.lyricsRepository,
            )
        }

    }
}

fun CreationExtras.theUltimateClockApplication(): LyricsApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LyricsApp)