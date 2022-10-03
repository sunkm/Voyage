package com.manchuan.tools.extensions

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.manchuan.tools.base.BottomSheet

fun AppCompatActivity.sheetDialog(
    binding: View, title: String,
    event: (View) -> Unit,
) {
    BottomSheet.initLayoutRes(binding)
    BottomSheet.title = title
    event(binding)
    BottomSheet().show(supportFragmentManager, title)
}

fun FragmentActivity.sheetDialog(
    binding: View, title: String,
    event: (View) -> Unit,
) {
    BottomSheet.initLayoutRes(binding)
    BottomSheet.title = title
    event(binding)
    BottomSheet().show(supportFragmentManager, title)
}