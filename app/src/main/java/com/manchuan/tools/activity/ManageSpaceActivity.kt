package com.manchuan.tools.activity

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.blankj.utilcode.util.FileUtils
import com.drake.statusbar.immersive
import com.manchuan.tools.R
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.databinding.ActivityManageSpaceBinding
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.io.File
import java.io.IOException

class ManageSpaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageSpaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive(binding.toolbar)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "管理空间"
        supportFragmentManager.beginTransaction()
            .replace(R.id.layout, SettingsFragment())
            .commit()
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { ViewPumpContextWrapper.wrap(it) })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(p1: Preference, p2: Any): Boolean {
            return false
        }

        override fun onSharedPreferenceChanged(p1: SharedPreferences, p2: String) {}

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.storage_preference)
            val allClearPreference = findPreference<Preference>("all_clear")
            val imageCachePreference = findPreference<Preference>("clear_image_cache")
            val exoCachePreference = findPreference<Preference>("clear_video_cache")
            val dataCachePreference = findPreference<Preference>("clear_data")
            val logCachePreference = findPreference<Preference>("clear_log")
            dataCachePreference?.summary =
                "已占大小:" + FileUtils.getSize(requireContext().dataDir.absolutePath)
            logCachePreference?.summary =
                "已占大小:" + FileUtils.getSize(requireContext().filesDir.absolutePath + File.separator + "tombstones")
            exoCachePreference?.summary =
                "已占大小:" + FileUtils.getSize(requireContext().cacheDir.absolutePath + File.separator + "exo")
            imageCachePreference?.summary =
                "已占大小:" + FileUtils.getSize(requireContext().cacheDir.absolutePath + File.separator + "image_manager_disk_cache")
            allClearPreference?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener { p1: Preference? ->
                    BaseAlertDialogBuilder(requireContext())
                        .setTitle("警告")
                        .setMessage("此操作不可撤销，这会清除软件的所有数据，且所有权限需要重新授予！")
                        .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                            ManageSpaceActivity().clearAppUserData(requireContext().packageName)
                        }
                        .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, i: Int ->

                        }
                        .create()
                        .show()
                    false
                }
        }
    }

    fun clearAppUserData(packageName: String): Process? {
        return execRuntimeProcess("pm clear $packageName")
    }

    private fun execRuntimeProcess(commond: String?): Process? {
        var p: Process? = null
        try {
            p = Runtime.getRuntime().exec(commond)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return p
    }


}