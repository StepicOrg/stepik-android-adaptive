package org.stepik.android.adaptive.pdd.data.model

import com.google.gson.annotations.SerializedName

data class AccountCredentials(@SerializedName("login")     val login       : String,
                              @SerializedName("password")  val password    : String,
                              @SerializedName("firstName") val firstName   : String,
                              @SerializedName("lastName")  val lastName    : String)