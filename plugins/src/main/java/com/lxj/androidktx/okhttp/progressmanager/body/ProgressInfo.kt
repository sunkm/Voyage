/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lxj.androidktx.okhttp.progressmanager.body

import android.os.Parcel
import android.os.Parcelable

/**
 * ================================================
 * [ProgressInfo] 用于存储与进度有关的变量,已实现 [Parcelable]
 *
 *
 * Created by JessYan on 07/06/2017 12:09
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class ProgressInfo : Parcelable {
    var currentbytes //当前已上传或下载的总长度
            : Long = 0
    var contentLength //数据总长度
            : Long = 0
    var intervalTime //本次调用距离上一次被调用所间隔的时间(毫秒)
            : Long = 0
    var eachBytes //本次调用距离上一次被调用的间隔时间内上传或下载的byte长度
            : Long = 0
    var id //如果同一个 Url 地址,上一次的上传或下载操作都还没结束,
            : Long
        private set

    //又开始了新的上传或下载操作(比如用户多次点击上传或下载同一个 Url 地址,当然你也可以在上层屏蔽掉用户的重复点击),
    //此 id (请求开始时的时间)就变得尤为重要,用来区分正在执行的进度信息,因为是以请求开始时的时间作为 id ,所以值越大,说明该请求越新
    var isFinish //进度是否完成
            = false

    constructor(id: Long) {
        this.id = id
    }

    /**
     * 获取百分比,该计算舍去了小数点,如果你想得到更精确的值,请自行计算
     *
     * @return
     */
    val percent: Int
        get() = if (currentbytes <= 0 || contentLength <= 0) 0 else (100 * currentbytes / contentLength).toInt()

    /**
     * 获取上传或下载网络速度,单位为byte/s,如果你想得到更精确的值,请自行计算
     *
     * @return
     */
    val speed: Long
        get() = if (eachBytes <= 0 || intervalTime <= 0) 0 else eachBytes * 1000 / intervalTime

    override fun toString(): String {
        return "ProgressInfo{" +
                "id=" + id +
                ", currentBytes=" + currentbytes +
                ", contentLength=" + contentLength +
                ", eachBytes=" + eachBytes +
                ", intervalTime=" + intervalTime +
                ", finish=" + isFinish +
                '}'
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(currentbytes)
        dest.writeLong(contentLength)
        dest.writeLong(intervalTime)
        dest.writeLong(eachBytes)
        dest.writeLong(id)
        dest.writeByte(if (isFinish) 1.toByte() else 0.toByte())
    }

    protected constructor(`in`: Parcel) {
        currentbytes = `in`.readLong()
        contentLength = `in`.readLong()
        intervalTime = `in`.readLong()
        eachBytes = `in`.readLong()
        id = `in`.readLong()
        isFinish = `in`.readByte().toInt() != 0
    }

    companion object CREATOR : Parcelable.Creator<ProgressInfo> {
        override fun createFromParcel(parcel: Parcel): ProgressInfo {
            return ProgressInfo(parcel)
        }

        override fun newArray(size: Int): Array<ProgressInfo?> {
            return arrayOfNulls(size)
        }
    }
}