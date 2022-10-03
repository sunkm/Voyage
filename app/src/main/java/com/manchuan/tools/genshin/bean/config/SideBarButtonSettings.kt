package com.lianyi.paimonsnotebook.bean.config

import com.manchuan.tools.genshin.GenshinConfig
import com.manchuan.tools.genshin.ext.sp
import com.manchuan.tools.genshin.untils.GSON


data class SideBarButtonSettings(val enabled:Boolean,val stylePreview:Boolean,val hideDefaultSideBarButton:Boolean,val widthProgress:Int,val sideBarAreaWidth:Int) {
    companion object{
        var instance: SideBarButtonSettings =
            GSON.fromJson(sp.getString(GenshinConfig.SP_SIDE_BAR_BUTTON_SETTINGS,""),SideBarButtonSettings::class.java)
                ?: SideBarButtonSettings(
                    enabled = true,
                    stylePreview = false,
                    hideDefaultSideBarButton = false,
                    widthProgress = 40,
                    sideBarAreaWidth = 40
                )
    }
    override fun toString(): String {
        return "SideBarButtonSettings(enabled=$enabled, stylePreview=$stylePreview, hideDefaultSideBarButton=$hideDefaultSideBarButton, widthProgress=$widthProgress, sideBarAreaWidth=$sideBarAreaWidth)"
    }
}