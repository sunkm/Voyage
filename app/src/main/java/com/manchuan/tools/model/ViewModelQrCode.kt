package com.manchuan.tools.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object ViewModelQrCode : ViewModel() {

    //是否可保存
    var isCanSave = MutableLiveData<Boolean>()

}