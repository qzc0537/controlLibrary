package com.rhizo.libentity

import android.os.Parcel
import android.os.Parcelable

data class PositionBean(
    var x: Float = 0F,
    var y: Float = 0F,
    var yaw: Float = 0F,
    var tiltAngle: Int = 0
) : Parcelable, BaseBody() {

    constructor(source: Parcel) : this(
        source.readFloat(),
        source.readFloat(),
        source.readFloat(),
        source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeFloat(x)
        writeFloat(y)
        writeFloat(yaw)
        writeInt(tiltAngle)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PositionBean> = object : Parcelable.Creator<PositionBean> {
            override fun createFromParcel(source: Parcel): PositionBean = PositionBean(source)
            override fun newArray(size: Int): Array<PositionBean?> = arrayOfNulls(size)
        }
    }
}

