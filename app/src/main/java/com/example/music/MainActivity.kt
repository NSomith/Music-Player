package com.example.music

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.music.adapters.SwipeSongAdapter
import com.example.music.data.entities.Song
import com.example.music.exoplayer.callback.isPlaying
import com.example.music.exoplayer.toSong
import com.example.music.other.Status
import com.example.music.ui.viewModel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel:MainViewModel by viewModels()

//    private lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var currentlyPlayingSong: Song? = null

    @Inject
    lateinit var glide: RequestManager

    private var playBackState:PlaybackStateCompat?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        Log.d("mainview","$mainViewModel")
        subscribetoObserver()
        vpSong.adapter = swipeSongAdapter
//        vpSong.orientation = ViewPager2.ORIENTATION_VERTICAL

        vpSong.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playBackState?.isPlaying == true){
                    mainViewModel.playORtoggle(swipeSongAdapter.songs[position])
                }else{
                    currentlyPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playORtoggle(it,true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(
                    R.id.globalactionToSongFrgment
            )
        }
        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.homeFragment->showBottomBar()
                R.id.songFragment->hideBottomBar()
            }
        }
    }

    private fun hideBottomBar(){
        vpSong.isVisible = false
        ivCurSongImage.isVisible =false
        ivPlayPause.isVisible = false
    }

    private fun showBottomBar(){
        vpSong.isVisible = true
        ivCurSongImage.isVisible =true
        ivPlayPause.isVisible = true
    }

    private fun swithcViewPagerToCurrentSong(song:Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1) {
            vpSong.currentItem = newItemIndex
            currentlyPlayingSong = song
        }
    }

    private fun subscribetoObserver(){
        mainViewModel.mediaItems.observe(this){
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

        mainViewModel.curPlayingSong.observe(this){
            if(it == null) return@observe
            currentlyPlayingSong = it.toSong()
            glide.load(currentlyPlayingSong?.imageUrl).into(ivCurSongImage)
            swithcViewPagerToCurrentSong(currentlyPlayingSong?: return@observe)
        }

        mainViewModel.playbackState.observe(this){
            playBackState =it
            ivPlayPause.setImageResource(
                    if(playBackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        mainViewModel.isConnected.observe(this){
            it?.getContentIfHandled()?.let {
              when(it.status) {
                  Status.ERROR -> Snackbar.make(
                          rootLayout,
                          it.message ?: "An unknown error occured",
                          Snackbar.LENGTH_LONG
                  ).show()

                  else->Unit
              }
            }
        }
        mainViewModel.networkError.observe(this){
            it?.getContentIfHandled()?.let {
                when(it.status) {
                    Status.ERROR -> Snackbar.make(
                            rootLayout,
                            it.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                    ).show()

                    else->Unit
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("mainview on destroy","$mainViewModel")
    }


}