package com.manchuan.tools.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alibaba.fastjson.JSON
import com.drake.net.Post
import com.drake.net.utils.scopeDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.FragmentXiaoMiBinding

class XiaoMiFragment : Fragment() {
    private var _binding: FragmentXiaoMiBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentXiaoMiBinding.inflate(inflater, container, false)
        binding.help.setOnClickListener {
            BaseAlertDialogBuilder(requireContext())
                .setTitle("帮助")
                .setMessage("1.为什么修改步数之后没有生效\n答：可能因为数据接口的原因，导致修改失败\n\n2.修改之后关联的其他平台会生效吗？\n答：会，支持同步到微信、支付宝、QQ、阿里体育")
                .setPositiveButton("确定", null)
                .show()
        }
        binding.editSteps.setOnClickListener {
            when {
                binding.account.text.toString().isEmpty() && binding.password.text.toString()
                    .isEmpty() && binding.steps.text.toString().isEmpty() -> {
                    binding.accountLay.error = "请填写小米运动账号"
                    binding.passwordLay.error = "请填写小米运动密码"
                    binding.stepsLay.error = "请填写步数"
                }
                binding.account.text.toString().isEmpty() -> {
                    binding.accountLay.error = "请填写小米运动账号"
                }
                binding.password.text.toString().isEmpty() -> {
                    binding.passwordLay.error = "请填写小米运动密码"
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
                                Post<String>("https://apibug.cn/api/xmstep/") {
                                    param("user", binding.account.text.toString())
                                    param("password", binding.password.text.toString())
                                    param("step", binding.steps.text.toString())
                                    param("apiKey", "e6bcddc57b97845f009743a1559daaaf")
                                }.await()
                            val json = JSON.parseObject(content)
                            if (json.getIntValue("code") == 200) {
                                PopTip.show("提交成功,${json.getString("message")}")
                            } else {
                                PopTip.show("提交失败,${json.getString("message")}")
                            }
                        }
                    }
                }
            }
        }
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}