package com.example.music.exoplayer.callback

import android.widget.Toast
import com.example.music.exoplayer.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player

//used for pause or resume
class MusicPlayerEventListener(
    val musicService: MusicService
) :Player.EventListener{
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady){
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService,"Error occured",Toast.LENGTH_LONG).show()
    }
}