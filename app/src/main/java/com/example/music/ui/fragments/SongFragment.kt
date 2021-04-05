package com.example.music.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.music.R
import com.example.music.data.entities.Song
import com.example.music.exoplayer.callback.isPlaying
import com.example.music.exoplayer.toSong
import com.example.music.other.Status
import com.example.music.ui.viewModel.MainViewModel
import com.example.music.ui.viewModel.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment:Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glid:RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel:SongViewModel by viewModels() //lifecycle of the SongFragment

    private var currPlayingSong: Song?=null
    private var playBackState:PlaybackStateCompat? = null
    private var shouldUpdateSeekBar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeTOObserver()

        ivPlayPauseDetail.setOnClickListener {
            currPlayingSong?.let {
                mainViewModel.playORtoggle(it,true)
            }
        }

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setCurrPlayerTimetoSeekbar(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }

        })

    }
    private fun updateTitleAndImg(song: Song){
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glid.load(song.imageUrl).into(ivSongImage)
    }

    private fun subscribeTOObserver(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let {
                when(it.status){
                    Status.SUCCESS->{
                        it.data?.let { songs->
                            if(currPlayingSong == null && songs.isNotEmpty()){
                                currPlayingSong = songs[0]
                                updateTitleAndImg(songs[0])
                            }
                        }
                    }
                    else->Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            if(it == null)return@observe
            currPlayingSong = it.toSong()
            updateTitleAndImg(currPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playBackState =it
            ivPlayPauseDetail.setImageResource(
                    if(playBackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        }
        songViewModel.currentPlayerPostion.observe(viewLifecycleOwner){
            if(shouldUpdateSeekBar){
                seekBar.progress = it.toInt()
                setCurrPlayerTimetoSeekbar(it)
            }
        }
        songViewModel.currentSongDuration.observe(viewLifecycleOwner){
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurrPlayerTimetoSeekbar(ms: Long?) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }

}