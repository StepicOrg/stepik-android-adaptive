package org.stepik.android.adaptive.util

import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

inline fun skipUIFrame(crossinline action: () -> Unit, delay: Long = 0) {
    Completable
            .timer(delay, TimeUnit.MICROSECONDS)
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
        action()
    }
}