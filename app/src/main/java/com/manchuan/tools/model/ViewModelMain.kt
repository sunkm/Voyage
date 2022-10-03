package com.manchuan.tools.model
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @功能: 用于和Service通信
 * @User Muchuan
 * @Create 4/19/22 6:10 PM
 * @Summary 永远相信美好的事情即将发生
 */
object ViewModelMain : ViewModel() {
    //悬浮窗口创建 移除
    var isShowWindow = MutableLiveData<Boolean>()
    //悬浮窗口创建 移除

    var isShowSuspendWindow = MutableLiveData<Boolean>()

    //悬浮窗口显示 隐藏
    var isVisible = MutableLiveData<Boolean>()

}