package com.manchuan.tools.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.manchuan.tools.R
import com.manchuan.tools.databinding.ActivityPrivateBinding

class PrivateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = "隐私政策"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(getColor(R.color.textColor))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.toolbarLayout.setCollapsedTitleTextColor(getColor(R.color.textColor))
        binding.toolbarLayout.setExpandedTitleColor(getColor(R.color.textColor))
    }

}