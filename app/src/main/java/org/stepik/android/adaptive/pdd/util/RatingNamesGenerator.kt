package org.stepik.android.adaptive.pdd.util

import android.content.Context
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr


object RatingNamesGenerator {
    private lateinit var context: Context

    private val animalsMale by lazy { context.resources.getStringArray(R.array.animals_m) }
    private val animalsFemale by lazy { context.resources.getStringArray(R.array.animals_f) }

    private val animals by lazy { animalsMale + animalsFemale }
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
                val hash = hash(user)
                val animal = animals[(hash % animals.size).toInt()]

                val adjIndex = (hash / animals.size).toInt()
                val adj = if (isFemaleNoun(animal)) {
                    adjectivesFemale[adjIndex]
                } else {
                    adjectives[adjIndex]
                }

                adj.capitalize() + ' ' + animal
            }

    @JvmStatic
    private fun isFemaleNoun(noun: String) = animalsFemale.contains(noun)

    @JvmStatic
    private fun hash(x: Long): Long {
        var h = x
        h = h.ushr(16).xor(h) * 0x45d9f3b
        h = h.ushr(16).xor(h) * 0x45d9f3b
        h = h.ushr(16).xor(h)
        return h % (animals.size * adjectives.size)
    }
}