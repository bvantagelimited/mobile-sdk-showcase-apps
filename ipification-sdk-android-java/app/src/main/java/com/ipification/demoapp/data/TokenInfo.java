package com.ipification.demoapp.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TokenInfo implements Parcelable {

    public boolean phoneNumberVerified;
    public String phoneNumber;
    public String loginHint;
    public String sub;
    public String mobileID;

    public TokenInfo(boolean phoneNumberVerified, String phoneNumber, String loginHint, String sub, String mobileID) {
        this.phoneNumberVerified = phoneNumberVerified;
        this.phoneNumber = phoneNumber;
        this.loginHint = loginHint;
        this.sub = sub;
        this.mobileID = mobileID;
    }

    protected TokenInfo(Parcel in) {
        phoneNumberVerified = in.readByte() != 0;
        phoneNumber = in.readString();
        loginHint = in.readString();
        sub = in.readString();
        mobileID = in.readString();
    }

    public static final Creator<TokenInfo> CREATOR = new Creator<TokenInfo>() {
        @Override
        public TokenInfo createFromParcel(Parcel in) {
            return new TokenInfo(in);
        }

        @Override
        public TokenInfo[] newArray(int size) {
            return new TokenInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (phoneNumberVerified ? 1 : 0));
        parcel.writeString(phoneNumber);
        parcel.writeString(loginHint);
        parcel.writeString(sub);
        parcel.writeString(mobileID);
    }
}
