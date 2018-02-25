package org.stepik.android.adaptive.util

import android.support.annotation.DrawableRes
import org.stepik.android.adaptive.BuildConfig
import org.stepik.android.adaptive.R
import org.stepik.android.adaptive.data.SharedPreferenceMgr

object InventoryUtil {
    enum class Item(val key: String, @DrawableRes val iconId: Int) {
        Ticket("tickets", R.drawable.ic_tickets)
//        Chest("chest", R.drawable.ic_chest)
    }

    enum class PaidContent(
            val id: String,
            val item: Item,
            val count: Int) {
        SmallCouponsPack("error_tickets_package_small", Item.Ticket, 7),
        MediumCouponsPack("error_tickets_package_medium", Item.Ticket, 15),
        BigCouponsPack("error_tickets_package_big", Item.Ticket, 30),
        MonsterCouponsPack("error_tickets_package_monster", Item.Ticket, 100);

        companion object {
            private val idToContent by lazy { values().associateBy { it.id } }

            val ids = idToContent.keys

            fun getById(id: String) = idToContent[id]
        }
    }

    private const val STARTER_PACK_VERSION_KEY = "starter_pack_version"

    private const val START_TICKETS_COUNT = 7L

    @JvmStatic
    fun getItemsCount(item: Item) =
        SharedPreferenceMgr.getInstance().getLong(item.key)

    private fun setItemsCount(item: Item, count: Long) =
          SharedPreferenceMgr.getInstance().saveLong(item.key, count)

    @JvmStatic
    fun useItem(item: Item) : Boolean {
        val count = getItemsCount(item)
        if (count > 0) {
            setItemsCount(item, count - 1)
            return true
        }
        return false
    }

    @JvmStatic
    fun changeItemCount(item: Item, delta: Long) =
        SharedPreferenceMgr.getInstance().changeLong(item.key, delta)

    @JvmStatic
    fun hasTickets() = getItemsCount(Item.Ticket) > 0

    @JvmStatic
    fun starterPack() {
        if (SharedPreferenceMgr.getInstance().getLong(STARTER_PACK_VERSION_KEY) == 0L) {
            SharedPreferenceMgr.getInstance().saveLong(STARTER_PACK_VERSION_KEY, BuildConfig.VERSION_CODE.toLong())
            setItemsCount(Item.Ticket, START_TICKETS_COUNT)
        }
    }

    @JvmStatic
    fun getInventory() : List<Pair<Item, Int>> =
        Item.values()
                .map { it to InventoryUtil.getItemsCount(it).toInt() }
                .filter { it.second > 0 }
                .toList()

}