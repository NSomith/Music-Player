package com.example.music

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.music.adapters.SwipeSongAdapter
import com.example.music.data.entities.Song
import com.example.music.exoplayer.toSong
import com.example.music.other.Status
import com.example.music.ui.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainviewModel:MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var currentlyPlayingSong: Song? = null

    @Inject
    lateinit var glide: RequestManager

    val smooth  = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribetoObserver()
        vpSong.adapter = swipeSongAdapter
//        vpSong.orientation = ViewPager2.ORIENTATION_VERTICAL
    }

    private fun swithcViewPagerToCurrentSong(song:Song){
        val newItemIdx = swipeSongAdapter.songs.indexOf(song)
        if(newItemIdx !=-1){
           vpSong.setCurrentItem(newItemIdx)
            vpSong.currentItem  = newItemIdx
        }
        Log.d("viewpager","call ${currentlyPlayingSong?.title}")
    }

    private fun subscribetoObserver(){
        mainviewModel.mediaItems.observe(this){
            it?.let {
               when(it.status){
                   Status.SUCCESS->{
                       it.data?.let { songs->
                           swipeSongAdapter.songs = songs
                           if(songs.isNotEmpty()){
                               glide.load((currentlyPlayingSong ?: songs[0]).imageUrl).into(ivCurSongImage)
                           }
                           swithcViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)
                       }
                   }
                   Status.LOADING->Unit
                   Status.ERROR->Unit
               }
            }
        }

        mainviewModel.curPlayingSong.observe(this){
            if(it == null) return@observe
            Log.d("viewpager","called")
            currentlyPlayingSong = it.toSong()
            glide.load(currentlyPlayingSong?.imageUrl).into(ivCurSongImage)
            swithcViewPagerToCurrentSong(currentlyPlayingSong?: return@observe)
        }
    }
}