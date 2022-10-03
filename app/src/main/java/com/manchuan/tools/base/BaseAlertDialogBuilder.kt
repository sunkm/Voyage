package com.manchuan.tools.base

import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.manchuan.tools.utils.OsUtils

class BaseAlertDialogBuilder : MaterialAlertDialogBuilder {
    constructor(context: Context) : super(context)
    constructor(context: Context, overrideThemeResId: Int) : super(context, overrideThemeResId)

    override fun create(): AlertDialog {
        return super.create().also { dialog ->
            if (OsUtils.atLeastS()) {
                dialog.apply {

                }
                dialog.window?.let {
                    it.returnTransition = MaterialFadeThrough()
                    it.exitTransition = MaterialFadeThrough()
                    it.enterTransition = MaterialFadeThrough()
                    it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    it.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    it.attributes.blurBehindRadius = 18
                }
            }
        }
    }
}