package com.manchuan.tools.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WallpaperModel(
    @SerialName("code") var code: Int,
    @SerialName("msg") var msg: String,
    @SerialName("res") var res: Res,
) {
    @Serializable
    data class Res(
        @SerialName("vertical") var vertical: List<Vertical>,
    ) {
        @Serializable
        data class Vertical(
            @SerialName("atime") var atime: Double,
            @SerialName("cid") var cid: List<String>,
            @SerialName("cr") var cr: Boolean,
            @SerialName("desc") var desc: String,
            @SerialName("favs") var favs: Int,
            @SerialName("id") var id: String,
            @SerialName("img") var img: String,
            @SerialName("ncos") var ncos: Int,
            @SerialName("preview") var preview: String,
            @SerialName("rank") var rank: Int,
            @SerialName("rule") var rule: String,
            @SerialName("source_type") var sourceType: String,
            @SerialName("store") var store: String,
            @SerialName("tag") var tag: List<String>,
            @SerialName("thumb") var thumb: String,
            @SerialName("url") var url: List<String>? = null,
            @SerialName("views") var views: Int,
            @SerialName("wp") var wp: String,
            @SerialName("xr") var xr: Boolean,
        )
    }
}