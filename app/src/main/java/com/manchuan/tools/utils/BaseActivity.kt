package com.manchuan.tools.utils

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        initBindView()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        initBindView()
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        super.setContentView(view, params)
        initBindView()
    }

    override fun addContentView(view: View, params: ViewGroup.LayoutParams) {
        super.addContentView(view, params)
        initBindView()
    }

    private fun initBindView() {
        val filelds = javaClass.declaredFields
        for (field in filelds) {
            if (field.isAnnotationPresent(BindView::class.java)) {
                val bind = field.getAnnotation(
                    BindView::class.java
                )
                if (bind != null) {
                    field.isAccessible = true
                    try {
                        field[this] = findViewById(bind.value)
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}