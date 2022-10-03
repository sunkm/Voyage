package com.manchuan.tools.genshin.base

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.manchuan.tools.genshin.`interface`.BackPressedListener

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //沉浸模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //内容扩展显示
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        //设置应用只能竖屏显示
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    //另一种fragment触发onBackPressed回调的方法
//    override fun onBackPressed() {
//        if(!interceptBackPressed()){
//            super.onBackPressed()
//        }
//    }

    private fun interceptBackPressed(): Boolean {
        supportFragmentManager.fragments.forEach {
            if (it is BackPressedListener) {
                if (it.handelBackPressed()) {
                    return true
                }
            }
        }
        return false
    }

}