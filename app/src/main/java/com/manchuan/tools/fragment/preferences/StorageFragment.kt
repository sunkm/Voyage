package com.manchuan.tools.fragment.preferences

import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.panpf.sketch.sketch
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.manchuan.tools.R
import com.manchuan.tools.activity.app.SettingsActivity
import com.manchuan.tools.base.BaseAlertDialogBuilder
import com.manchuan.tools.extensions.formatSize
import java.io.File

class StorageFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.storage_preference, rootKey)
        val allClearPreference = findPreference<Preference>("all_clear")
        val imageCachePreference = findPreference<Preference>("clear_image_cache")
        val exoCachePreference = findPreference<Preference>("clear_video_cache")
        val dataCachePreference = findPreference<Preference>("clear_data")
        val logCachePreference = findPreference<Preference>("clear_log")
        dataCachePreference?.summary =
            "已占大小:" + formatSize(requireContext().dataDir.absolutePath)
        logCachePreference?.summary =
            "已占大小:" + formatSize(requireContext().filesDir.absolutePath + File.separator + "tombstones")
        exoCachePreference?.summary =
            "已占大小:" + formatSize(requireContext().cacheDir.absolutePath + File.separator + "exo")
        imageCachePreference?.summary =
            "已占大小:" + formatSize(requireContext().sketch.memoryCache.size)
        allClearPreference?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { p1: Preference? ->
                BaseAlertDialogBuilder(requireContext()).setTitle("警告")
                    .setMessage("此操作不可撤销，这会清除软件的所有数据，且所有权限需要重新授予！")
                    .setPositiveButton(android.R.string.ok) { dialogInterface: DialogInterface, i: Int ->
                        context?.packageName?.let { SettingsActivity().clearAppUserData(it) }
                    }
                    .setNegativeButton(android.R.string.cancel) { dialogInterface: DialogInterface, i: Int ->

                    }.create().show()
                false
            }
    }
}