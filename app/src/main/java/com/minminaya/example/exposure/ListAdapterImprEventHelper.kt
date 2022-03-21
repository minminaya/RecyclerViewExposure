package com.minminaya.example.exposure

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.minminaya.example.adapter.ItemRecyclerViewListAdapter
import com.minminaya.example.fragment.placeholder.PlaceholderContent
import com.minminaya.exposure.AbsListAdapterImprEventHelper

class ListAdapterImprEventHelper(
    recyclerView: RecyclerView,
    fragment: Fragment
) : AbsListAdapterImprEventHelper<ItemRecyclerViewListAdapter,
        PlaceholderContent.PlaceholderItem>(
    recyclerView,
    fragment
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
            "ListAdapterImprEventHelper",
            "onItemExposure:---- absoluteAdapterPosition:$absoluteAdapterPosition ,$entity"
        )
    }

}