package com.example.music.ui.viewModel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.exoplayer.MusicService
import com.example.music.exoplayer.MusicServiceConnection
import com.example.music.exoplayer.callback.currentPlaybackPostion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(
        val musicServiceConnection: MusicServiceConnection
) :ViewModel(){

    private val playbackState = musicServiceConnection.playbackState

    private val _currentSongDuration = MutableLiveData<Long>()
    val currentSongDuration:LiveData<Long> = _currentSongDuration

    private val _currentPlayerPostion = MutableLiveData<Long>()
    val currentPlayerPostion:LiveData<Long> = _currentPlayerPostion

    init {
        updateCurrentPlayerPostion()
    }

    private fun updateCurrentPlayerPostion(){
        viewModelScope.launch {
            while (true){
                val pos = playbackState.value?.currentPlaybackPostion
                if(_currentPlayerPostion.value != pos){
                    _currentPlayerPostion.postValue(pos)
                    _currentSongDuration.postValue(MusicService.currSongDuration)
                }
            }
            delay(100L)
        }
    }
}