package com.minminaya.example.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minminaya.example.R
import com.minminaya.example.adapter.ItemRecyclerViewListAdapter
import com.minminaya.example.fragment.placeholder.PlaceholderContent
import com.minminaya.exposure.AbsListAdapterImprEventHelper

class ListAdapterExampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        val recyclerview = findViewById<RecyclerView>(R.id.list)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val listAdapter = ItemRecyclerViewListAdapter()
        recyclerview.adapter = listAdapter
        listAdapter.submitList(PlaceholderContent.ITEMS)
        //抽象类初始化
        object :
            AbsListAdapterImprEventHelper<ItemRecyclerViewListAdapter, PlaceholderContent.PlaceholderItem>(
                recyclerview,
                this
            ) {
            override fun needPostEvent(entity: PlaceholderContent.PlaceholderItem): Boolean {
                return true
            }

            override fun onItemExposure(
                entity: PlaceholderContent.PlaceholderItem,
                absoluteAdapterPosition: Int,
                bindingAdapterPosition: Int
            ) {
                Log.d(
                    "ListAdapterExampleActivity",
                    "onItemExposure:---- absoluteAdapterPosition:$absoluteAdapterPosition ,$entity"
                )
            }

        }
    }
}