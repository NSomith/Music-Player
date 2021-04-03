package com.example.music.exoplayer.callback

import android.util.Log
import android.widget.Toast
import com.example.music.exoplayer.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player

//used for pause or resume
class MusicPlayerEventListener(
    private val musicService: MusicService/* We need this access to musicService because we
        need to stop the foreground service from within this listener */
) :Player.EventListener{

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        Log.d("onPlayerStateChanged","the value of $playWhenReady")
        if(playbackState == Player.STATE_READY && !playWhenReady){
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Log.d("viewpager","${error}")
        Toast.makeText(musicService,"Error occured${error}",Toast.LENGTH_LONG).show()
    }
}