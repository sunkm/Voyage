package com.manchuan.tools.plugins.api

import com.manchuan.tools.plugins.api.layout.LayoutArgument

interface Layout<out T> {

    abstract fun callLayout(argument: LayoutArgument):T?

    abstract val name:String

    abstract val id:String


}