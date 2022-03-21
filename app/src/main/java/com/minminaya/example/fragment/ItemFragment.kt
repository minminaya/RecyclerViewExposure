package com.minminaya.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minminaya.example.R
import com.minminaya.example.adapter.ItemRecyclerViewListAdapter
import com.minminaya.example.exposure.ListAdapterImprEventHelper
import com.minminaya.example.fragment.placeholder.PlaceholderContent

/**
 * A fragment representing a list of Items.
 */
class ItemFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.list)
        with(rv) {
            layoutManager = LinearLayoutManager(context)
            val listAdapter = ItemRecyclerViewListAdapter()
            adapter = listAdapter
            listAdapter.submitList(PlaceholderContent.ITEMS)
        }
        ListAdapterImprEventHelper(rv, this)
    }

    companion object {

        @JvmStatic
        fun newInstance() = ItemFragment()

    }
}