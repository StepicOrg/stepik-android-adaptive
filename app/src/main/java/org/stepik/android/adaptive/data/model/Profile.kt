package org.stepik.android.adaptive.data.model

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
)