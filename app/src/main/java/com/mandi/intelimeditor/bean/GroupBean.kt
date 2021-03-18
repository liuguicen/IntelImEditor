package com.mathandintell.intelimedit.bean

import android.os.Parcel
import android.os.Parcelable
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource

/**
 * 图片分组
 * @param title 分组名
 */
data class GroupBean(val title: String, var heat: Int) : Parcelable {

    constructor(title: String, data: List<PicResource>) : this(title, 0) {
        data.forEach {
            heat += it.heat
        }
    }

    constructor(parcel: Parcel) : this(
            parcel.readString().toString(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeInt(heat)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupBean> {
        override fun createFromParcel(parcel: Parcel): GroupBean {
            return GroupBean(parcel)
        }

        override fun newArray(size: Int): Array<GroupBean?> {
            return arrayOfNulls(size)
        }
    }
}