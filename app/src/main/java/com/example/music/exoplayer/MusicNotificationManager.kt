package com.example.music.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.music.R
import com.example.music.other.Utility.NOTIFICATION_CHANNEL_ID
import com.example.music.other.Utility.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManager(
    val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,/* This is a listener that
        contains functions that can be used after the notification is created e.g. when user swipes
        away the notification, we need to stop the foreground service (media) */

    val newSongCallback:()->Unit/* Here we can detect when a new song starts playing.
        It can be used to set current duration of the song*/
) {
    private val notificationManger:PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context,sessionToken)
        notificationManger = PlayerNotificationManager.createWithNotificationChannel(
                context,
                NOTIFICATION_CHANNEL_ID,
                R.string.notification_channel_name,
                R.string.notification_channel_des,
                NOTIFICATION_ID,
                DescriptionAdapter(mediaController),
                notificationListener
        ).apply {
            setSmallIcon(R.drawable.ic_music)
            setMediaSessionToken(sessionToken)/*This will give our notificationManager access to
            current media in our media service*/
        }
    }

    fun showNotification(player: Player){
        notificationManger.setPlayer(player)
    }

    private inner class DescriptionAdapter(
            private val mediaController:MediaControllerCompat
    ):PlayerNotificationManager.MediaDescriptionAdapter{
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }

//        load the Glide so as to load the img in bitmap form can return null or callback when the img is ready
        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            Glide.with(context).asBitmap()
                    .load(mediaController.metadata.description.iconUri)
                    .into(object :CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            callback.onBitmap(resource) //when our imgae is loaded since it a callback
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            return null
        }

    }
}