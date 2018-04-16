package org.stepik.android.adaptive.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import org.stepik.android.adaptive.api.profile.model.EmailAddress

class Profile (
    val id: Long = 0,
    @SerializedName("first_name")
    var firstName: String? = null,
    @SerializedName("last_name")
    var lastName: String? = null,
    @SerializedName("full_name")
    var fullName: String? = null,
    var avatar: String? = null,
    @SerializedName("subscribed_for_mail")
    var subscribedForMail: Boolean? = null,
    @SerializedName("email_addresses")
    var emailAddresses: LongArray = longArrayOf(),
    var emailAddressesResolved: List<EmailAddress> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
            parcel.createLongArray(),
            parcel.createTypedArrayList(EmailAddress))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(fullName)
        parcel.writeString(avatar)
        parcel.writeValue(subscribedForMail)
        parcel.writeLongArray(emailAddresses)
        parcel.writeTypedList(emailAddressesResolved)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile = Profile(parcel)
        override fun newArray(size: Int): Array<Profile?> = arrayOfNulls(size)
    }
}