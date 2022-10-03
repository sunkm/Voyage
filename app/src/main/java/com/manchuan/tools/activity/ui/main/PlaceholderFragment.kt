package com.manchuan.tools.activity.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSON
import com.drake.net.Get
import com.drake.net.utils.scopeDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.activity.WebActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.FragmentStepsBinding

class PlaceholderFragment : Fragment() {
    private var _binding: FragmentStepsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStepsBinding.inflate(inflater, container, false)
        val root = binding.root
        binding.getToken.setOnClickListener {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(
                "url",
                "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=1101326786&daid=381&pt_skey_valid=1&style=35&s_url=http://connect.qq.com&refer_cgi=m_authorize&ucheck=1&fall_to_wv=1&status_os=9.3.2&redirect_uri=auth://www.qq.com&client_id=1101326786&response_type=token&scope=all&sdkp=i&sdkv=2.9&state=test&status_machine=iPhone8,1&switch=1"
            )
            startActivity(intent)
        }
        binding.help.setOnClickListener {
            BaseAlertDialogBuilder(requireContext())
                .setTitle("帮助")
                .setMessage("1.为什么修改步数之后没有生效\n答：可能因为数据接口的原因，导致修改失败\n\n2.修改之后关联的其他平台会生效吗？\n答：会，但只支持同步到QQ")
                .setPositiveButton("确定", null)
                .show()
        }
        binding.editSteps.setOnClickListener {
            when {
                binding.account.text.toString().isEmpty() && binding.password.text.toString()
                    .isEmpty() && binding.steps.text.toString().isEmpty() -> {
                    binding.accountLay.error = "请填写ID"
                    binding.passwordLay.error = "请填写TOKEN"
                    binding.stepsLay.error = "请填写步数"
                }
                binding.account.text.toString().isEmpty() -> {
                    binding.accountLay.error = "请填写ID"
                }
                binding.password.text.toString().isEmpty() -> {
                    binding.passwordLay.error = "请填写TOKEN"
                }
                binding.steps.text.toString().isEmpty() -> {
                    binding.stepsLay.error = "请填写步数"
                }
                else -> {
                    if (binding.steps.text.toString().toInt() > 100000) {
                        PopTip.show("步数最多只能100000步")
                    } else {
                        scopeDialog {
                            val content =
                                Get<String>("https://api.iyk0.com/ydq/?openid=${binding.account.text.toString()}&steps=${binding.steps.text.toString()}&access_token=${binding.password.text.toString()}").await()
                            PopTip.show(JSON.parseObject(content).getString("msg"))
                        }
                    }
                }
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}