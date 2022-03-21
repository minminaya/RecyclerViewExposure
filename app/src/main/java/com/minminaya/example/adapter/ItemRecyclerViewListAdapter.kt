package com.minminaya.example.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import asConfig
import com.minminaya.example.databinding.FragmentItemBinding
import com.minminaya.example.fragment.placeholder.PlaceholderContent.PlaceholderItem

class ItemRecyclerViewListAdapter : ListAdapter<PlaceholderItem, RecyclerView.ViewHolder>(
    PlaceholderItem.entityComparator.asConfig()
) {

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
        val item = getItem(position)
        (holder as? ViewHolder)?.apply {
            idView.text = item.id
            contentView.text = item.content
        }
    }


}