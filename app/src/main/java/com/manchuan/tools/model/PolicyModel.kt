package com.manchuan.tools.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class PolicyModel(var isAcceptPolicy: Boolean = false,var isNeverAsk: Boolean = false) : Parcelable {

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(parcel.readBoolean()) // 读取空则赋值默认值

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeBoolean(isAcceptPolicy)
        parcel.writeBoolean(isNeverAsk)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PolicyModel> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): PolicyModel {
            return PolicyModel(parcel)
        }

        override fun newArray(size: Int): Array<PolicyModel?> {
            return arrayOfNulls(size)
        }
    }
}