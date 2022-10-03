package com.manchuan.tools.extensions

import android.os.Environment


inline val externalMoviesDirPath: String?
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)?.absolutePath

inline val externalAudiosDirPath: String?
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)?.absolutePath

inline val externalPicturesDirPath: String?
    get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)?.absolutePath
