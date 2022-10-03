package com.manchuan.tools.model

import com.manchuan.tools.interfaces.HardwareFloat
import com.manchuan.tools.interfaces.HardwareFloatInterface
import com.manchuan.tools.interfaces.HardwarePresenter
import org.koin.dsl.module

val hardwareModel = module {
    single<HardwareFloatInterface> {
        HardwareFloat()
    }
    factory {
        HardwarePresenter(get())
    }
}

val appModels = listOf(hardwareModel)