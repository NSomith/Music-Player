package com.example.music.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.music.data.entities.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(it.mediaId?:"",
                it.title.toString(),
                it.subtitle.toString(),
                it.iconUri.toString(),
                it.mediaUri.toString()
        )
    }
}