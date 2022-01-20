package com.ipification.demoapp.data

import android.os.Parcel
import android.os.Parcelable

class TokenInfo(
    val phoneNumberVerified: Boolean,
    val phoneNumber: String?,
    val loginHint: String?,
    val sub: String?,
    val mobileID: String?
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readInt() == 1,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {

        parcel.writeInt(if(phoneNumberVerified) 1 else 0)
        parcel.writeString(phoneNumber)
        parcel.writeString(loginHint)
        parcel.writeString(sub)
        parcel.writeString(mobileID)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TokenInfo> {
        override fun createFromParcel(parcel: Parcel): TokenInfo {
            return TokenInfo(parcel)
        }

        override fun newArray(size: Int): Array<TokenInfo?> {
            return arrayOfNulls(size)
        }
    }
}
