package org.stepik.android.adaptive.gamification

import androidx.annotation.DrawableRes
import org.stepik.android.adaptive.BuildConfig
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.preference.SharedPreferenceHelper
import org.stepik.android.adaptive.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class InventoryManager
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {
    companion object {
        private const val STARTER_PACK_VERSION_KEY = "starter_pack_version"
        private const val START_TICKETS_COUNT = 7L
    }

    enum class Item(val key: String, @DrawableRes val iconId: Int) {
        Ticket("tickets", R.drawable.ic_coupon_pack_small)
//        Chest("chest", R.drawable.ic_chest)
    }

    enum class PaidContent(
        val id: String,
        val item: Item,
        val count: Int,
        @DrawableRes val icon: Int
    ) {
        SmallCouponsPack("error_tickets_package_small", Item.Ticket, 7, R.drawable.ic_coupon_pack_small),
        MediumCouponsPack("error_tickets_package_medium", Item.Ticket, 15, R.drawable.ic_coupon_pack_medium),
        BigCouponsPack("error_tickets_package_big", Item.Ticket, 30, R.drawable.ic_coupon_pack_big),
        MonsterCouponsPack("error_tickets_package_monster", Item.Ticket, 100, R.drawable.ic_coupon_pack_monster);

        companion object {
            private val idToContent by lazy { values().associateBy { it.id } }

            val ids = idToContent.keys

            fun getById(id: String): PaidContent? =
                idToContent[id]
        }
    }

    fun getItemsCount(item: Item): Long =
        sharedPreferenceHelper.getLong(item.key)

    private fun setItemsCount(item: Item, count: Long) {
        sharedPreferenceHelper.saveLong(item.key, count)
    }

    fun useItem(item: Item): Boolean {
        val count = getItemsCount(item)
        if (count > 0) {
            setItemsCount(item, count - 1)
            return true
        }
        return false
    }

    fun changeItemCount(item: Item, delta: Long): Long =
        sharedPreferenceHelper.changeLong(item.key, delta)

    fun hasTickets(): Boolean =
        getItemsCount(Item.Ticket) > 0

    fun starterPack() {
        if (sharedPreferenceHelper.getLong(STARTER_PACK_VERSION_KEY) == 0L) {
            sharedPreferenceHelper.saveLong(STARTER_PACK_VERSION_KEY, BuildConfig.VERSION_CODE.toLong())
            setItemsCount(Item.Ticket, START_TICKETS_COUNT)
        }
    }

    fun getInventory(): List<Pair<Item, Int>> =
        Item.values()
            .map { it to getItemsCount(it).toInt() }
//                .filter { it.second > 0 }
            .toList()
}
