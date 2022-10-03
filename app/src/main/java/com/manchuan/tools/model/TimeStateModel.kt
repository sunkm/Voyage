package com.manchuan.tools.model

import androidx.lifecycle.SavedStateHandle
import com.drake.serialize.model.StateViewModel
import com.drake.serialize.model.stateHandle

class TimeStateModel(stateHandle: SavedStateHandle) : StateViewModel(stateHandle) {
    var name: String by stateHandle()
}