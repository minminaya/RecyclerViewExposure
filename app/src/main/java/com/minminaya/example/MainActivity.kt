package com.minminaya.example

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minminaya.example.activity.ListAdapterExampleActivity
import com.minminaya.example.activity.RecyclerAdapterExampleActivity
import com.minminaya.example.databinding.ActivityMainBinding
import com.minminaya.example.fragment.FragmentContainerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFragmentExampleBtnClickListener()
        setExampleListAdapterClickListener()
        setRecyclerAdapterExampleClickListener()
    }

    private fun setFragmentExampleBtnClickListener() {
        binding.fragmentExampleBtn.setOnClickListener {
            Intent(this, FragmentContainerActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setExampleListAdapterClickListener() {
        binding.activityExampleListAdapterBtn.setOnClickListener {
            Intent(this, ListAdapterExampleActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    private fun setRecyclerAdapterExampleClickListener() {
        binding.activityExampleRvAdapterBtn.setOnClickListener {
            Intent(this, RecyclerAdapterExampleActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

}