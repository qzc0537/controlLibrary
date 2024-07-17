package com.rhizo.libcontrol.bean

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

data class WifiBean(
    var ssid: String = "",
    var capabilities: String = "",
    var password: String = "",
    var ipAddress: String = "",
    var port: Int = 0,
    var serialNo: String = "",
    var level: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this() {
        ssid = parcel.readString() ?: ""
        capabilities = parcel.readString() ?: ""
        password = parcel.readString() ?: ""
        ipAddress = parcel.readString() ?: ""
        port = parcel.readInt()
        serialNo = parcel.readString() ?: ""
        level = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(ssid)
        dest.writeString(capabilities)
        dest.writeString(password)
        dest.writeString(ipAddress)
        dest.writeInt(port)
        dest.writeString(serialNo)
        dest.writeInt(level)
    }

    val isCorrectIp: Boolean
        get() = ipAddress != "0.0.0.0"

    companion object {
        const val INVALID_SSID = "<unknown ssid>"

        @JvmField
        val CREATOR: Creator<WifiBean> = object : Creator<WifiBean> {
            override fun createFromParcel(`in`: Parcel): WifiBean {
                return WifiBean(`in`)
            }

            override fun newArray(size: Int): Array<WifiBean?> {
                return arrayOfNulls(size)
            }
        }
    }
}
