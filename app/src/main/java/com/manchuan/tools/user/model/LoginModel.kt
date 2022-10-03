package com.manchuan.tools.user.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginModel(
    @SerialName("code")
    var code: Int,
    @SerialName("msg")
    var msg: Msg,
    @SerialName("time")
    var time: Int
) {
    @Serializable
    data class Msg(
        @SerialName("info")
        var info: Info,
        @SerialName("token")
        var token: String
    ) {
        @Serializable
        data class Info(
            @SerialName("fen")
            var fen: String,
            @SerialName("id")
            var id: String,
            @SerialName("name")
            var name: String,
            @SerialName("pic")
            var pic: String,
            @SerialName("vip")
            var vip: String
        )
    }
}