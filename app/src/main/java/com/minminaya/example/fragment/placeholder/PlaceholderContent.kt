package com.minminaya.example.fragment.placeholder

import androidx.recyclerview.widget.DiffUtil
import com.minminaya.exposure.IEntityForImpr

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, PlaceholderItem> = HashMap()

    private val COUNT = 1000

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i))
        }
    }

    private fun addItem(item: PlaceholderItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createPlaceholderItem(position: Int): PlaceholderItem {
        return PlaceholderItem(position.toString(), "Item " + position, makeDetails(position))
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val id: String, val content: String, val details: String) :
        IEntityForImpr {

        override fun toString(): String = content

        override fun getIdForImpr(): String {
            return id
        }

        companion object {
            val entityComparator =
                object : DiffUtil.ItemCallback<PlaceholderItem>() {

                    override fun areItemsTheSame(
                        oldItem: PlaceholderItem,
                        newItem: PlaceholderItem
                    ): Boolean {
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(
                        oldItem: PlaceholderItem,
                        newItem: PlaceholderItem
                    ): Boolean =
                        oldItem == newItem
                }
        }
    }
}