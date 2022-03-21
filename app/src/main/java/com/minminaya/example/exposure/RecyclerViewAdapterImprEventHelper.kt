package com.minminaya.example.exposure

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.minminaya.example.adapter.ItemRecyclerViewAdapter
import com.minminaya.example.fragment.placeholder.PlaceholderContent
import com.minminaya.exposure.AbsListImprEventHelper

class RecyclerViewAdapterImprEventHelper(
    recyclerView: RecyclerView,
    componentActivity: ComponentActivity
) : AbsListImprEventHelper<ItemRecyclerViewAdapter,
        PlaceholderContent.PlaceholderItem>(
    recyclerView,
    componentActivity
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
            "Event",
            "onItemExposure:---- absoluteAdapterPosition:$absoluteAdapterPosition ,$entity"
        )
    }

    override fun onBatchItemExposure(tripleList: MutableList<Triple<PlaceholderContent.PlaceholderItem, Int, Int>>) {
        super.onBatchItemExposure(tripleList)
        Log.d(
            "Event",
            "onBatchItemExposure:---- tripleList:$tripleList"
        )
    }

    override fun getAdapterEntityForPosition(
        bindingAdapterPosition: Int,
        viewHolder: RecyclerView.ViewHolder
    ): PlaceholderContent.PlaceholderItem? {
        //自定义返回 Adapter 中某个 item 对应的数据
        return (viewHolder.bindingAdapter as? ItemRecyclerViewAdapter)?.let {
            if (bindingAdapterPosition in 0 until it.dataList.size) {
                return@let it.dataList[bindingAdapterPosition]
            } else null
        }
    }

}