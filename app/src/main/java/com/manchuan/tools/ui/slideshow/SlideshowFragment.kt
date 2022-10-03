package com.manchuan.tools.ui.slideshow

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.manchuan.tools.R

class SlideshowFragment : Fragment() {
    @SuppressLint("InlinedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.activity_settings, container, false)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.list_container, SettingsFragment())
            .commit()
        super.onCreateView(inflater, container, savedInstanceState)
        return root
    }

    class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(p1: Preference, p2: Any): Boolean {
            return false
        }

        override fun onSharedPreferenceChanged(p1: SharedPreferences, p2: String) {}
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            //setTheme(R.style.PreferenceThemeOverlay);
            addPreferencesFromResource(R.xml.settings_perference)
        }
    }
}