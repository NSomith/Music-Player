package com.example.music.data.remote

import android.util.Log
import com.example.music.data.entities.Song
import com.example.music.other.Utility.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    val firestore = FirebaseFirestore.getInstance()
    val songcollection = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs():List<Song>{
        return try {
            songcollection.get().await().toObjects(Song::class.java)
        }catch (e:Exception){
            Log.d("database","$e")
            emptyList()
        }
    }
}