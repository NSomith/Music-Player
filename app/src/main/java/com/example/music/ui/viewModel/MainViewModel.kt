package com.example.music.ui.viewModel
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music.data.entities.Song
import com.example.music.exoplayer.MusicServiceConnection
import com.example.music.exoplayer.callback.isPlayEnable
import com.example.music.exoplayer.callback.isPlaying
import com.example.music.exoplayer.callback.isPrepared
import com.example.music.other.Resource
import com.example.music.other.Utility.MEDIA_ROOT_ID

class MainViewModel @ViewModelInject constructor(
    val musicServiceConnection: MusicServiceConnection
):ViewModel() {

    private val _mediaItems =  MutableLiveData<Resource<List<Song>>>()
    val mediaItems : LiveData<Resource<List<Song>>> = _mediaItems


    val isConnected = musicServiceConnection.isconnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.currPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID,object :
            MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }
    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos:Long){
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playORtoggle(medaiItem:Song,toggle:Boolean = false){
        val isPrepared = playbackState.value?.isPrepared ?:false
        if (isPrepared && medaiItem.mediaId == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnable -> musicServiceConnection.transportControls.play() //when pasue the song
                    else -> Unit
                }
            }
        }
        else {
            musicServiceConnection.transportControls.playFromMediaId(medaiItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){})
    }
}