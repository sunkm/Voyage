package com.manchuan.tools.genshin.bean.config

import com.manchuan.tools.genshin.GenshinConfig
import com.manchuan.tools.genshin.ext.sp
import com.manchuan.tools.genshin.untils.GSON

data class ContentMarginSettings(val enable:Boolean,val marginPreview:Boolean,val horizontalProgress:Int,val horizontalMargin:Int,val topProgress:Int,val topMargin:Int) {

    companion object{
        var instance = GSON.fromJson(sp.getString(GenshinConfig.SP_CONTENT_MARGIN_SETTINGS,""),
            ContentMarginSettings::class.java)
            ?: ContentMarginSettings(
                enable = false,
                marginPreview = false,
                horizontalProgress = 0,
                horizontalMargin = 0,
                topProgress = 0,
                topMargin = 0
            )
    }
}