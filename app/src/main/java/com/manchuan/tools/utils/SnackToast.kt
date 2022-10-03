package com.manchuan.tools.utils

import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.manchuan.tools.R

/**
 * @author ManChuan
 */
object SnackToast {
    fun makeText(content: String?, view: View?, color: Int) {
        // TODO: Implement this method
        val snack = Snackbar.make(view!!, content!!, Snackbar.LENGTH_SHORT)
        (snack.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView).setTextColor(-0x1)
        snack.setBackgroundTint(color)
        snack.show()
    }

    @JvmStatic
    fun makeText(content: String?, view: View?) {
        // TODO: Implement this method
        val snackbar = Snackbar.make(view!!, content!!, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(-0xdededf)
        (snackbar.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView).setTextColor(-0x1)
        snackbar.view.setBackgroundResource(R.drawable.snackbar_radius)
        snackbar.show()
    }

    fun makeTextAndAction(
        content: String?,
        view: View?,
        actionText: String?,
        actionClick: View.OnClickListener?
    ) {
        // TODO: Implement this method
        val snack = Snackbar.make(view!!, content!!, Snackbar.LENGTH_SHORT)
        snack.setBackgroundTint(-0x10dededf)
        (snack.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView).setTextColor(-0x1)
        snack.view.setBackgroundResource(R.drawable.snackbar_radius)
        snack.setAction(actionText, actionClick)
        snack.show()
    }
}