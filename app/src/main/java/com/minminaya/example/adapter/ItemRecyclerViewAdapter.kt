package com.minminaya.example.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minminaya.example.databinding.FragmentItemBinding
import com.minminaya.example.fragment.placeholder.PlaceholderContent

class ItemRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val dataList = mutableListOf<PlaceholderContent.PlaceholderItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        (holder as? ViewHolder)?.apply {
            idView.text = item.id
            contentView.text = item.content
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}