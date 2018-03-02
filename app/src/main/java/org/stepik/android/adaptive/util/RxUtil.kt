package org.stepik.android.adaptive.util

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

inline fun skipUIFrame(crossinline action: () -> Unit, delay: Long = 0) {
    Completable
            .timer(delay, TimeUnit.MICROSECONDS)
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
        action()
    }
}

inline fun <T, R> Observable<T>.mapNotNull(crossinline f: (T) -> R?): Observable<R> = flatMap {
    f(it)?.let { return@let Observable.just(it) } ?: Observable.empty()
}