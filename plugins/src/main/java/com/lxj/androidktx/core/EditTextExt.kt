package com.lxj.androidktx.core

import android.text.InputFilter
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText

/**
 * 显示密码文本
 */
fun EditText.showPassword(){
    transformationMethod = HideReturnsTransformationMethod.getInstance()
    setSelection(text.length)
}

/**
 * 隐藏密码文本
 */
fun EditText.hidePassword(){
    transformationMethod = PasswordTransformationMethod.getInstance()
    setSelection(text.length)
}

/**
 * 动态设置最大长度限制
 */
fun EditText.maxLength(max: Int){
    filters = arrayOf<InputFilter>(InputFilter.LengthFilter(max))
}

fun EditText.setTextWidthEndCursor(s: CharSequence){
    setText(s)
    setSelection(text.toString().length)
}