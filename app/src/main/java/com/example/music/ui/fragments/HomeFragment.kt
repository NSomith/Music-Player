package com.example.music.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music.R
import com.example.music.adapters.SongAdapter
import com.example.music.other.Status
import com.example.music.ui.viewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment:Fragment(R.layout.fragment_home) {

//     val mainViewModel:MainViewModel by viewModels()

    private lateinit var mainViewModel:MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d("fragment","$mainViewModel")
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        Log.d("fragment view","$mainViewModel")
        setupRecylerView()
        subscribeToObservers()
        songAdapter.setItemClickListener {
            mainViewModel.playORtoggle(it)
        }
    }

    fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){result->
            when(result.status){
                Status.LOADING->{
                    allSongsProgressBar.isVisible = true
                }
                Status.SUCCESS->{
                    allSongsProgressBar.isVisible = false
                    result.data?.let {
                        songAdapter.songs = it
                    }
                }
                Status.ERROR->Unit //becz we didnt write for error part
            }

        }
    }

    fun setupRecylerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager =LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("fragment viewdestroy","$mainViewModel")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("fragment destroy","$mainViewModel")

    }
}