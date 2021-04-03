package com.example.music.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.music.R
import com.example.music.data.entities.Song
import kotlinx.android.synthetic.main.list_item.view.*

abstract class BaseSongAdapter(
        private val layoutId:Int
) : RecyclerView.Adapter<BaseSongAdapter.SongViewHolder>(){

    class SongViewHolder(itemview: View): RecyclerView.ViewHolder(itemview)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        layoutId,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    protected val diffutilcallback = object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract val differ:AsyncListDiffer<Song>

    protected var onItemClickListener:((Song)->Unit)? = null

    fun setItemClickListener(listener:(Song)->Unit){
        onItemClickListener = listener
    }

    var songs:List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)
}