package com.example.top10downloaderapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.top10downloaderapp.databinding.ItemViewBinding

class RVAdapter (private val informationList: ArrayList<Group>): RecyclerView.Adapter<RVAdapter.ItemViewHolder>() {

    private lateinit var myListener: OnItemClickListener

    class ItemViewHolder(val binding: ItemViewBinding, listener: OnItemClickListener ): RecyclerView.ViewHolder(binding.root){

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener:OnItemClickListener ){
        myListener=listener
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),myListener)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val title = informationList[position].title


        holder.binding.apply {
            titleTV.text = "Title: $title"
            titleTV.setTextColor(Color.MAGENTA)

        }
    }

    override fun getItemCount() = informationList.size

}