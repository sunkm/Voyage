package com.manchuan.tools.activity.transfer

import android.os.Bundle
import com.lxj.androidktx.core.startActivity
import com.manchuan.tools.base.AnimationActivity
import com.manchuan.tools.databinding.ActivityTransferFileBinding

class TransferFileActivity : AnimationActivity() {
    private val binding by lazy {
        ActivityTransferFileBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.button2.setOnClickListener {
            startActivity<SendFileActivity>()
        }
        binding.button3.setOnClickListener {
            startActivity<ReceiveActivity>()
        }
    }
}