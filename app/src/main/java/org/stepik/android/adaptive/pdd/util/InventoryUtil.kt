package org.stepik.android.adaptive.pdd.util

import org.stepik.android.adaptive.pdd.BuildConfig
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr

object InventoryUtil {
    @JvmStatic
    val TICKETS_KEY = "tickets"

    private val STARTER_PACK_VERSION_KEY = "starter_pack_version"

    private val START_TICKETS_COUNT = 10L

    @JvmStatic
    fun getItemsCount(key: String) =
        SharedPreferenceMgr.getInstance().getLong(key)

    private fun setItemsCount(key: String, count: Long) =
          SharedPreferenceMgr.getInstance().saveLong(key, count)

    @JvmStatic
    fun useItem(key: String) : Boolean {
        val count = getItemsCount(key)
        if (count > 0) {
            setItemsCount(key, count - 1)
            return true
        }
        return false
    }

    @JvmStatic
    fun hasTickets() = getItemsCount(TICKETS_KEY) > 0

    @JvmStatic
    fun starterPack() {
        if (SharedPreferenceMgr.getInstance().getLong(STARTER_PACK_VERSION_KEY) == 0L) {
            SharedPreferenceMgr.getInstance().saveLong(STARTER_PACK_VERSION_KEY, BuildConfig.VERSION_CODE.toLong())
            setItemsCount(TICKETS_KEY, START_TICKETS_COUNT)
        }
    }
}