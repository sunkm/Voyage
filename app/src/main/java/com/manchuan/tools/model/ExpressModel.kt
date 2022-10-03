package com.manchuan.tools.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpressModel(
    @SerialName("com")
    var com: String,
    @SerialName("condition")
    var condition: String,
    @SerialName("data")
    var `data`: List<Data>,
    @SerialName("ischeck")
    var ischeck: String,
    @SerialName("message")
    var message: String,
    @SerialName("nu")
    var nu: String,
    @SerialName("state")
    var state: String,
    @SerialName("status")
    var status: String
) {
    @Serializable
    data class Data(
        @SerialName("context")
        var context: String,
        @SerialName("ftime")
        var ftime: String,
        @SerialName("time")
        var time: String
    )
}