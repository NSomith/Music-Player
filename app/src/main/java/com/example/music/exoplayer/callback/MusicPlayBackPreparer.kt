package com.example.music.exoplayer.callback

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.example.music.FirebaseMusicSource
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

/* This class provides us with the functions that will be called on player events such as:
when the ExoPlayer (Music Player) is prepared */
class MusicPlayBackPreparer(
    val firebaseMusicSource: FirebaseMusicSource,
    val playerPrepared:(MediaMetadataCompat?)->Unit //Gives out metadata of current track
) :MediaSessionConnector.PlaybackPreparer{
    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean {
        return false
    }

    override fun getSupportedPrepareActions(): Long {
//        return the type of action suported in our player
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) {}

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
//       prepare the music when the id/music is ready
//        playWhenReady means play it or when use play it
        firebaseMusicSource.whenReady {
            Log.d("mytag","onPrepareFromMediaId")
            val itemToPlay = firebaseMusicSource.songs.find {
                mediaId == it.description.mediaId
            }
            playerPrepared(itemToPlay)
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {

    }

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
    }

}