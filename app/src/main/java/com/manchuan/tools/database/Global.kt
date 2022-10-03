package com.manchuan.tools.database

import com.drake.serialize.serialize.serialLazy
import com.drake.serialize.serialize.serialLiveData
import com.manchuan.tools.activity.movies.model.MovieCategorys
import com.manchuan.tools.model.QQInfo
import com.manchuan.tools.model.QQLogin
import com.manchuan.tools.model.UserModel

object Global {

    var customFont: Boolean by serialLazy(false)
    var defaultFontPath: String by serialLazy("")
    var defaultFontName: String by serialLazy("")
    var isEnabledDefaultFont: Boolean by serialLazy(false)
    val textDayOne by serialLiveData(true)
    var countLaunch: Int by serialLazy(0)
    val textDailyGone by serialLiveData(false)
    var isFirst: Boolean by serialLazy(true)
    const val authority = "com.manchuan.tools.fileprovider"
    const val AppId = "102024802"
    var firebaseToken: String by serialLazy()
    var messageToken: String by serialLazy()
    var localMovieCategories: List<MovieCategorys.Data.DataList.Filter> by serialLazy()
    var localSentence: String by serialLazy("永远相信美好的事情即将发生")
    var localSourcesVersion: Int by serialLazy(0)
    var cachePolicyEnabled: Boolean by serialLazy(true)
    var idVerify: String by serialLazy()
    val userModel by serialLiveData(UserModel("", "", "", "", "", ""))
    var qqUserModel: QQInfo by serialLazy()
    var loginModel: QQLogin? by serialLazy()

}