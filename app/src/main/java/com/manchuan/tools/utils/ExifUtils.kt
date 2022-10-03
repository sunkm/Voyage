package com.manchuan.tools.utils

import android.media.ExifInterface
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.lang.NullPointerException

/**
 * 创建于 2021/3/28.
 */
object ExifUtils {
    private var exif: ExifInterface? = null
    var isSetPath = false
    fun setPath(file: String) {
        if (file.isEmpty()) {
            isSetPath = false
            throw NullPointerException("The file path must be set first.")
        } else {
            try {
                exif = ExifInterface(file)
                isSetPath = true
            } catch (e: IOException) {
            }
        }
    }

    //设置exif
    fun setExif(
        orientation: String?,
        time: String?,
        make: String?,
        model: String?,
        flash: String?,
        length: String?,
        width: String?,
        latitude: String?,
        longitude: String?,
        exposure: String?,
        apertrue: String?,
        iso: String?,
        digitized: String?,
        altitude: String?
    ) {
        if (isSetPath) {
            exif!!.setAttribute(ExifInterface.TAG_ORIENTATION, orientation) //旋转角度
            exif!!.setAttribute(ExifInterface.TAG_DATETIME, time) //拍摄时间
            exif!!.setAttribute(ExifInterface.TAG_MAKE, make) //设备品牌
            exif!!.setAttribute(ExifInterface.TAG_MODEL, model) //设备型号
            exif!!.setAttribute(ExifInterface.TAG_FLASH, flash) //闪光灯
            exif!!.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, length) //图片高度
            exif!!.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, width) //图片宽度
            exif!!.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitude) //维度
            exif!!.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitude) //经度
            exif!!.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exposure) //曝光时间
            exif!!.setAttribute(ExifInterface.TAG_APERTURE, apertrue) //光圈值
            exif!!.setAttribute(ExifInterface.TAG_ISO, iso) //ISO感光度
            exif!!.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, digitized) //数字化时间
            exif!!.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, altitude) //海拔
        } else {
            throw NullPointerException("The file path must be set first.")
        }
    }

    fun saveAttributes() {
        try {
            exif!!.saveAttributes() //最后保存起来
        } catch (e: IOException) {
            Timber.e(e, "cannot save exif")
        }
    }

    val orientation: String?
        get() {
            val orientation: String? = if (exif!!.getAttribute(ExifInterface.TAG_ORIENTATION)!!
                    .isEmpty() and (exif!!.getAttribute(ExifInterface.TAG_ORIENTATION) == "0")
            ) {
                "未定义"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_ORIENTATION)
            }
            return orientation
        }
    val dateTime: String?
        get() {
            val date: String? = if (exif!!.getAttribute(ExifInterface.TAG_DATETIME) == null) {
                "无"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_DATETIME)
            }
            return date
        }
    val make: String?
        get() {
            val make: String? = if (exif!!.getAttribute(ExifInterface.TAG_MAKE) == null) {
                "无"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_MAKE)
            }
            return make
        }
    val model: String?
        get() {
            val model: String? = if (exif!!.getAttribute(ExifInterface.TAG_MODEL) == null) {
                "无"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_MODEL)
            }
            return model
        }
    val flash: String?
        get() {
            val flash: String? = if (exif!!.getAttribute(ExifInterface.TAG_FLASH) == null) {
                "无"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_FLASH)
            }
            return flash
        }
    val length: String?
        get() {
            val length: String? = if (exif!!.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) == null) {
                "无"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
            }
            return length
        }
    val width: String?
        get() {
            val width: String? = if (exif!!.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) == null) {
                "无"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
            }
            return width
        }
    val latitude: String?
        get() {
            val latitude: String? = if (exif!!.getAttribute(ExifInterface.TAG_GPS_LATITUDE) == null) {
                    "无"
                } else {
                    exif!!.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                }
            return latitude
        }
    val longitude: String?
        get() {
            val longitude: String? = if (exif!!.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) == null) {
                    "无"
                } else {
                    exif!!.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
                }
            return longitude
        }
    val exposure: String?
        get() {
            val exposure: String? = if (exif!!.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) == null) {
                    "无"
                } else {
                    exif!!.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                }
            return exposure
        }
    val apertrue: String?
        get() {
            val apertrue: String? = if (exif!!.getAttribute(ExifInterface.TAG_APERTURE) == null) {
                    "无"
                } else {
                    exif!!.getAttribute(ExifInterface.TAG_APERTURE)
                }
            return apertrue
        }
    val iso: String?
        get() {
            val iso: String? = if (exif!!.getAttribute(ExifInterface.TAG_ISO) == null) {
                "无"
            } else {
                exif!!.getAttribute(ExifInterface.TAG_ISO)
            }
            return iso
        }
    val digitized: String?
        get() {
            val digitized: String? = if (exif!!.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED) == null) {
                    "无"
                } else {
                    exif!!.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
                }
            return digitized
        }
    val alititude: String?
        get() {
            val altitude: String? = if (exif!!.getAttribute(ExifInterface.TAG_GPS_ALTITUDE) == null) {
                    "无"
                } else {
                    exif!!.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)
                }
            return altitude
        }

    //获取exif
    fun getExif(filepath: String?): ExifInterface? {
        try {
            exif =
                ExifInterface(filepath!!) //想要获取相应的值：exif.getAttribute("对应的key")；比如获取时间：exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return exif
    }
}