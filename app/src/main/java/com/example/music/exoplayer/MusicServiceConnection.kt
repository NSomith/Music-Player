package com.example.music.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.BoolRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.music.other.Event
import com.example.music.other.Resource
import com.example.music.other.Utility.NETWORK_ERROR
//This class sits between Activity/Fragment and Service and helps them connect and communicate
class MusicServiceConnection(
    context:Context
) {
    private val _isconnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isconnected:LiveData<Event<Resource<Boolean>>> = _isconnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError:LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState:LiveData<PlaybackStateCompat?> = _playbackState

    private val _currPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currPlayingSong:LiveData<MediaMetadataCompat?> = _currPlayingSong

    lateinit var mediaController:MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context,MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect() //call the onConnected() fun in MediaBrowserConnectionCallback
    }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls //  fills that void, giving you methods to trigger any action
    // (including custom actions specific to your media playback such as ‘skip forward 30 seconds’). All of which directly trigger the
    // methods in your MediaSessionCompat.Callback in your Service
    /* using get() here because the transportControls
         are not instantiated yet ( observe that the var mediaController is a lateinit var (line 26) )
         therefore, we only want to access it when a value is actually provided otherwise the code
         will crash! */

//    to subscribe to a perticular media id
    fun subscribe(parentId:String,callback:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId,callback)
    }

    fun unsubscribe(parentId:String,callback:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId,callback)
    }

    private inner class MediaBrowserConnectionCallback(
       val context: Context
    ):MediaBrowserCompat.ConnectionCallback(){

        override fun onConnected() {
            mediaController = MediaControllerCompat(context,mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isconnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _isconnected.postValue(Event(Resource.error("Connection suspended",false)))
        }

        override fun onConnectionFailed() {
            _isconnected.postValue(Event(Resource.error("connection failed",false)))
        }
    }

    private inner class MediaControllerCallback:MediaControllerCompat.Callback(){
        /* This function is called whenever the state of our music player changes and it updates
        * the current value(or state) to _playbackState*/
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event){
                NETWORK_ERROR->_networkError.postValue(
                    Event(
                        Resource.error(
                            "Couldnt connect to the server",
                            null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

}