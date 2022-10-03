package com.manchuan.tools.json.movie.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieJson(
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("success")
    var success: Boolean
) {
    @Serializable
    data class Data(
        @SerialName("info")
        var info: Info,
        @SerialName("title")
        var title: String
    ) {
        @Serializable
        data class Info(
            @SerialName("imgurl")
            var imgurl: String,
            @SerialName("pingfen")
            var pingfen: String,
            @SerialName("pingjia")
            var pingjia: String,
            @SerialName("url")
            var url: String,
            @SerialName("yanyuan")
            var yanyuan: String
        )
    }
}