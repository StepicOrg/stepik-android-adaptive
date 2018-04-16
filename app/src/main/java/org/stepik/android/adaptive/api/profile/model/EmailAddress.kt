package org.stepik.android.adaptive.api.profile.model

import android.os.Parcel
import android.os.Parcelable

class EmailAddress(
        val id: Long? = null,
        val user: Long,
        val email: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readLong(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeLong(user)
        parcel.writeString(email)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<EmailAddress> {
        override fun createFromParcel(parcel: Parcel): EmailAddress = EmailAddress(parcel)
        override fun newArray(size: Int): Array<EmailAddress?> = arrayOfNulls(size)
    }
}