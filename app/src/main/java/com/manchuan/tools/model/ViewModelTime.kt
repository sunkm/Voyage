package com.manchuan.tools.model

import android.view.animation.Interpolator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object ViewModelTime : ViewModel() {
    //插值器改变
    var interpolator = MutableLiveData<Interpolator>()

}