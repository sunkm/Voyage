package com.manchuan.tools.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object ViewModelCovid : ViewModel() {

    var covidData = MutableLiveData<CovidTag>()

}