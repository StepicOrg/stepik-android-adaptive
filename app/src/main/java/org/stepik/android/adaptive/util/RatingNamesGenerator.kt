package org.stepik.android.adaptive.util

import android.content.Context
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class RatingNamesGenerator
@Inject
constructor(
    private val context: Context,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    private val animalsMale by lazy { context.resources.getStringArray(R.array.animals_m) }
    private val animalsFemale by lazy { context.resources.getStringArray(R.array.animals_f) }

    private val animals by lazy { animalsMale + animalsFemale }
    private val adjectives by lazy { context.resources.getStringArray(R.array.adjectives) }
    private val adjectivesFemale by lazy { context.resources.getStringArray(R.array.adjectives_female) }

    fun getName(user: Long): String =
        if (user == sharedPreferenceHelper.profileId) {
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

    private fun isFemaleNoun(noun: String) =
        animalsFemale.contains(noun)

    private fun hash(x: Long): Long {
        var h = x
        h = h.shr(16).xor(h) * 0x45d9f3b
        h = h.shr(16).xor(h) * 0x45d9f3b
        h = h.shr(16).xor(h)
        return h % (animals.size * adjectives.size)
    }
}
