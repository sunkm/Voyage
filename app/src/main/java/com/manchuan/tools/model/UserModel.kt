package com.manchuan.tools.model

import java.io.Serializable


data class UserModel(
    var token: String?,
    var id: String?,
    var avatar: String?,
    var name: String?,
    var vip: String?,
    var fen: String?,
) : Serializable