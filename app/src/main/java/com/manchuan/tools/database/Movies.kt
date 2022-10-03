package com.manchuan.tools.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "Movies", primaryKeys = ["title"])
data class Movies(
    @ColumnInfo(name = "title")
    var title : String,
    @ColumnInfo(name = "url")
    var url : String
)