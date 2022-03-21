package com.minminaya.exposure

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * 抽象曝光基类，如果列表 Adapter 是继承自 ListAdapter 可以使用该类简化曝光操作
 *
 * @param L : ListAdapter<T, VH>
 * @param T : Any
 * @constructor
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
abstract class AbsListAdapterImprEventHelper<L : ListAdapter<T, RecyclerView.ViewHolder>, T : IEntityForImpr> :
    AbsListImprEventHelper<L, T> {

    constructor(
        recyclerView: RecyclerView,
        componentActivity: ComponentActivity
    ) : super(recyclerView, componentActivity)

    constructor(
        recyclerView: RecyclerView,
        fragment: Fragment
    ) : super(recyclerView, fragment)

    @Suppress("UNCHECKED_CAST", "IMPLICIT_NOTHING_TYPE_ARGUMENT_AGAINST_NOT_NOTHING_EXPECTED_TYPE")
    override fun getAdapterEntityForPosition(
        bindingAdapterPosition: Int,
        viewHolder: RecyclerView.ViewHolder
    ): T? {
        return (viewHolder.bindingAdapter as? L)?.let {
            it.currentList.run {
                if (bindingAdapterPosition >= this.size) {
                    return null
                }
                return this[bindingAdapterPosition]
            }
        }
    }

}