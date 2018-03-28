package org.stepik.android.adaptive.util

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

data class RxOptional<out T>(val value: T?) {
    fun <R> map(f: (T) -> R?) =
            RxOptional(value?.let(f))
}

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

infix fun CompositeDisposable.addDisposable(d: Disposable) = this.add(d)