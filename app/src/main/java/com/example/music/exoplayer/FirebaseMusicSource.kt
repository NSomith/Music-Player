package com.example.music

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.music.State.*
import com.example.music.data.remote.MusicDatabase
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

//This class will get the list of songs that we got from FireStore DB
class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
){
    private val readylisteners = mutableListOf<(Boolean)->Unit>() //set the all the source
    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        state = STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()
        songs = allSongs.map { song->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
                .build()
        }
        state = STATE_INITIALIZED
    }

//    play second song as soon as first song is over so use ConcatenatingMediaSource
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

//    for playlist or album or simple song
    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private var state:State = STATE_CREATE
        set(value) {
            if(value== STATE_INITIALIZED || value ==STATE_ERROR){
                synchronized(readylisteners){
                    field = value //field is new value and value is updated value
                    readylisteners.forEach { listener->
                        listener(state ==STATE_INITIALIZED)
                    }
                }
            }else{
                field = value
            }
        }

    fun whenReady(action:(Boolean)->Unit):Boolean{
        if(state == STATE_CREATE|| state == STATE_INITIALIZING){
            readylisteners+=action
            return false //music source not ready so add to the list
        }else{
            action(state == STATE_INITIALIZED)
            return true //music source ready
        }
    }
}
//These states depict the current state of our DataSource
// (since we need an immediate result whether the data is ready or not in our app)
enum class State{
    STATE_CREATE,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}