package com.minminaya.example.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minminaya.example.R
import com.minminaya.example.adapter.ItemRecyclerViewAdapter
import com.minminaya.example.exposure.RecyclerViewAdapterImprEventHelper
import com.minminaya.example.fragment.placeholder.PlaceholderContent

class RecyclerAdapterExampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        val recyclerview = findViewById<RecyclerView>(R.id.list)
        recyclerview.layoutManager = LinearLayoutManager(this)
        val adapter = ItemRecyclerViewAdapter()
        recyclerview.adapter = adapter
        adapter.dataList.addAll(PlaceholderContent.ITEMS)
        adapter.notifyDataSetChanged()
        RecyclerViewAdapterImprEventHelper(recyclerview, this)
    }
}