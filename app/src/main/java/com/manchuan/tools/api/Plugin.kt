package com.manchuan.tools.api

interface Plugin {
    fun onLoad(out: Bean?)
}