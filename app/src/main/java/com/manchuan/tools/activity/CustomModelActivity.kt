package com.manchuan.tools.activity

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import com.blankj.utilcode.util.ZipUtils
import com.drake.statusbar.immersive
import com.kongzue.dialogx.dialogs.PopTip
import com.manchuan.tools.databinding.ActivityCustomModelBinding
import com.manchuan.tools.utils.AssetsUtil
import com.manchuan.tools.utils.BuildUtils
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import rikka.material.app.MaterialActivity
import java.io.File
import java.nio.charset.Charset

class CustomModelActivity : MaterialActivity() {
    private var customModelBinding: ActivityCustomModelBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customModelBinding = ActivityCustomModelBinding.inflate(LayoutInflater.from(this))
        setContentView(customModelBinding?.root)
        setSupportActionBar(customModelBinding?.toolbar)
        immersive(customModelBinding?.toolbar!!)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (!File(cacheDir.absolutePath + "module.zip").exists()) {
            AssetsUtil.copyFileFromAssets(this,"Template.zip",cacheDir.absolutePath,"module.zip")
            ZipUtils.unzipFile(cacheDir.absolutePath + File.separator + "module.zip",cacheDir.absolutePath + File.separator + "module")
        }
        supportActionBar?.title = "Magisk机型修改模块生成"
        customModelBinding?.model?.setText(BuildUtils.model)
        customModelBinding?.name?.setText(BuildUtils.name)
        customModelBinding?.brand?.setText(BuildUtils.brand)
        customModelBinding?.materialbutton1?.setOnClickListener {
            runCatching {
                writeModuleProp(customModelBinding?.model?.text.toString())
                writeSystemProp(customModelBinding?.model?.text.toString(),customModelBinding?.brand?.text.toString(),customModelBinding?.name?.text.toString())
                val zipFile = ZipFile(Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOCUMENTS + File.separator + "Magisk_" + customModelBinding?.model?.text.toString() + "型号.zip")
                zipFile.charset = Charset.forName("GBK")
                val zipParameters = ZipParameters()
                zipParameters.compressionMethod = CompressionMethod.DEFLATE
                zipParameters.compressionLevel = CompressionLevel.NORMAL
                zipFile.addFiles(listOf(File(cacheDir.absolutePath + File.separator + "module" + File.separator + "module.prop"),File(cacheDir.absolutePath + File.separator + "module" + File.separator + "system.prop")))
                zipFile.addFolder((File(cacheDir.absolutePath + File.separator + "module" + File.separator + "META-INF")))
            }.onSuccess {
                PopTip.show("已保存到手机存储空间/Documents")
            }
            //ZipUtils.zipFiles(File(cacheDir.absolutePath + File.separator + "module").listFiles(),Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOCUMENTS + File.separator + "Magisk_" + customModelBinding?.model?.text.toString() + "型号.zip")
        }
    }

    private fun writeModuleProp(device: String){
        val info = StringBuffer()
        info.append("id=phoneInfo")
        info.append("\nname=$device 手机型号")
        info.append("\nversion=1.0")
        info.append("\nversionCode=9999")
        info.append("\nauthor=Magisk机型修改模块生成 by Flipped")
        info.append("\ndescription=此模块让你的手机伪装成 $device 手机型号")
        info.append("\nminMagisk=1500")
        File(cacheDir.absolutePath + File.separator + "module" + File.separator + "module.prop").writeText(
            info.toString()
        )
    }

    private fun writeSystemProp(model: String,brand: String,name: String){
        val info = StringBuffer()
        info.append("ro.product.model=$model")
        info.append("\nro.product.brand=$brand")
        info.append("\nro.product.name=$name")
        File(cacheDir.absolutePath + File.separator + "module" + File.separator + "system.prop").writeText(
            info.toString()
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}