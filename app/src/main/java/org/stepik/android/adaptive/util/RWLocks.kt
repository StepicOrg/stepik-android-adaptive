package org.stepik.android.adaptive.util

import java.util.concurrent.locks.ReentrantReadWriteLock

object RWLocks {
    val DatabaseLock = ReentrantReadWriteLock()
}