package com.manchuan.tools.interfaces

import com.manchuan.tools.model.UserModel

interface OnUserListener {
    fun onSuccess(token: String, content: UserModel)
    fun onFail(content: String?)
}