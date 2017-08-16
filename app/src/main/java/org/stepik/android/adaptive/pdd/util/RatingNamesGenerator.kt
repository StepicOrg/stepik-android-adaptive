package org.stepik.android.adaptive.pdd.util

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr
import android.os.Build.VERSION.SDK_INT
import org.stepik.android.adaptive.pdd.Util
import java.util.*


object RatingNamesGenerator {
    private lateinit var context: Context

    private val animals by lazy { context.resources.getStringArray(R.array.animals) }
    private val adjectives by lazy { context.resources.getStringArray(R.array.adjectives) }
    private val adjectivesFemale by lazy { context.resources.getStringArray(R.array.adjectives_female) }

    @JvmStatic
    fun init(context: Context) {
        this.context = context
    }


    @JvmStatic
    fun getName(user: Long) : String =
            if (user == SharedPreferenceMgr.getInstance().profileId) {
                context.getString(R.string.rating_you_placeholder)
            } else {
                val animal = animals[getAnimalIndex(user)]
                val adj = if (animal.endsWith('а')) { // russian letter а
                    adjectivesFemale[getAdjectiveIndex(user)]
                } else {
                    adjectives[getAdjectiveIndex(user)]
                }

                adj.capitalize() + ' ' + animal
            }

    @JvmStatic
    fun getAnimalIndex(user: Long) =
            Util.getRandomNumberBetween(0, animals.size - 1)

    @JvmStatic
    fun getAdjectiveIndex(user: Long) =
            Util.getRandomNumberBetween(0, adjectives.size - 1)

}