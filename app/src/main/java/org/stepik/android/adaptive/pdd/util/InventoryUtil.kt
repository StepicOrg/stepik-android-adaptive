package org.stepik.android.adaptive.pdd.util

import android.support.annotation.DrawableRes
import org.stepik.android.adaptive.pdd.BuildConfig
import org.stepik.android.adaptive.pdd.R
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr

object InventoryUtil {
    enum class Item(val key: String, @DrawableRes val iconId: Int) {
        Ticket("tickets", R.drawable.ic_tickets),
        Chest("chest", R.drawable.ic_chest)
    }

    private val STARTER_PACK_VERSION_KEY = "starter_pack_version"

    private val START_TICKETS_COUNT = 7L

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