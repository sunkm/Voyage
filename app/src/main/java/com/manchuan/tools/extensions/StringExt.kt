package com.manchuan.tools.extensions

import ando.file.core.FileSizeUtils
import java.io.File

fun formatSize(path: String): String {
    return FileSizeUtils.formatFileSize(FileSizeUtils.getFileSize(File(path)))
}

fun formatSize(size: Long): String {
    return FileSizeUtils.formatFileSize(size)
}