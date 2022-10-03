package com.manchuan.tools.fragment.preferences

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.blankj.utilcode.util.AppUtils
import com.drake.serialize.serialize.deserialize
import com.drake.serialize.serialize.serialize
import com.dylanc.longan.asActivity
import com.dylanc.longan.design.snackbar
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.manchuan.tools.R
import com.manchuan.tools.database.Global
import com.manchuan.tools.fonts.FontUi
import com.manchuan.tools.utils.OsUtils
import com.manchuan.tools.utils.SettingsLoader
import com.manchuan.tools.vip.VipCheck
import rikka.material.app.DayNightDelegate
import rikka.material.preference.MaterialSwitchPreference
import rikka.preference.SimpleMenuPreference

class MainSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_perference, rootKey)
        val themePreference = findPreference<Preference>("theme_style") as ListPreference?
        val glidePreference = findPreference<Preference>("glide_cache") as ListPreference?
        val cachePreference = findPreference<Preference>("clear_cache")
        val loginPreference = findPreference<Preference>("vip")
        val refreshPreference = findPreference<SimpleMenuPreference>("text_refresh")
        val fontPreference = findPreference<MaterialSwitchPreference>("font_settings")
        val dynamicColors = findPreference<MaterialSwitchPreference>("dynamicColors")
        themePreference!!.summary = themePreference.entry
        glidePreference!!.summary = glidePreference.entry
        refreshPreference?.summary = refreshPreference?.entry
        fontPreference?.isChecked = Global.isEnabledDefaultFont
        cachePreference?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { p1: Preference? ->
                false
            }
        fontPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                when (newValue as Boolean) {
                    true -> {
                        if (FontUi.isEmptyFont()) {
                            context?.let { FontUi.downloadFont(it) }
                        } else {
                            Global.isEnabledDefaultFont = true
                            context?.asActivity()?.snackbar("应用新更改需要重新启动", "确定") {
                                AppUtils.relaunchApp(true)
                            }
                        }
                    }

                    false -> {
                        context?.asActivity()?.snackbar("应用新更改需要重新启动", "确定") {
                            AppUtils.relaunchApp(true)
                        }
                    }
                }
                true
            }
        loginPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            context?.let {
                when (VipCheck.isLogin()) {
                    true -> VipCheck.infoUi(it.asActivity()!!)
                    false -> VipCheck.loginUi(it.asActivity()!!)
                }
            }
            false
        }
        if (OsUtils.atLeastS()) {
            dynamicColors?.setDefaultValue(true)
        } else {
            dynamicColors?.setDefaultValue(false)
            dynamicColors?.isEnabled = false
        }
        dynamicColors?.isChecked = deserialize("isEnabledDynamicColors", OsUtils.atLeastS())
        dynamicColors?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                serialize("isEnabledDynamicColors" to newValue)
                AppUtils.relaunchApp(true)
                true
            }
        themePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val indexOfValue = themePreference.findIndexOfValue(newValue.toString())
                themePreference.summary =
                    if (indexOfValue >= 0) themePreference.entries[indexOfValue] else null
                //restartApp();
                SettingsLoader.nightMode?.let { DayNightDelegate.setDefaultNightMode(it) }
                context?.asActivity()?.snackbar("应用新更改需要重新启动", "确定") {
                    AppUtils.relaunchApp(true)
                }
                true
            }
        refreshPreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val indexOfValue = refreshPreference?.findIndexOfValue(newValue.toString())!!
                refreshPreference.summary =
                    if (indexOfValue >= 0) refreshPreference.entries?.get(indexOfValue) else null
                when (newValue.toString()) {
                    "self" -> {
                        Global.textDailyGone.value = false
                        Global.textDayOne.value = false
                    }

                    "day_one" -> {
                        Global.textDailyGone.value = false
                        Global.textDayOne.value = true
                    }

                    "gone" -> {
                        Global.textDailyGone.value = true
                    }
                }
                true
            }
        glidePreference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                val indexOfValue = glidePreference.findIndexOfValue(newValue.toString())
                glidePreference.summary =
                    if (indexOfValue >= 0) glidePreference.entries[indexOfValue] else null
                when (newValue.toString()) {
                    "disabled" -> Global.cachePolicyEnabled = false
                    "enabled" -> Global.cachePolicyEnabled = true
                }
                true
            }
    }
}