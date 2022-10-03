package com.manchuan.tools.plugins.api.context

import android.content.Context
import android.content.res.AssetManager
import com.manchuan.tools.plugins.api.Plugin
import com.manchuan.tools.plugins.api.Properties
import com.manchuan.tools.plugins.api.layout.LayoutService

interface PluginContext {
    fun getLayoutService(): LayoutService

    fun getAssetManager(plugin: Plugin): AssetManager

    fun getProperties(): Properties


    /**
     * Return the base context based on the plugin, the context can only be used to get assets and resource resources, not to used create view
     */
    fun getAndroidContext(): Context

    val apiVersion: Int
        get() = 1

}