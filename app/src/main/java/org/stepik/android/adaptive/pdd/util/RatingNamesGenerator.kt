package org.stepik.android.adaptive.pdd.util

import android.content.Context
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr

object RatingNamesGenerator {
    private lateinit var context: Context

    @JvmStatic
    fun init(context: Context) {
        this.context = context
    }


    @JvmStatic
    fun getName(user: Long) =
            if (user == SharedPreferenceMgr.getInstance().profileId) {
                context.getString(R.string.rating_you_placeholder)
            } else {
                user.toString()
            }

}