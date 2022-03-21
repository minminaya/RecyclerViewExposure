package com.minminaya.example.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minminaya.example.R

class FragmentContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)
        var fragment =
            supportFragmentManager.findFragmentByTag(ItemFragment::class.java.canonicalName)
        if (fragment == null) {
            fragment = ItemFragment.newInstance()
            val beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.add(
                android.R.id.content,
                fragment,
                ItemFragment::class.java.canonicalName
            )
            beginTransaction.commitAllowingStateLoss()
        }

    }
}