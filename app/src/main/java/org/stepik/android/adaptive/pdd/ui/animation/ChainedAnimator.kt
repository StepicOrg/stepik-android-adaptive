package org.stepik.android.adaptive.pdd.ui.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter

class ChainedAnimator(private val c: () -> Animator) {
    private var prev : ChainedAnimator? = null
    private var next : ChainedAnimator? = null

    private var block : (() -> Unit)? = null

    fun then(block: () -> Animator) = then(ChainedAnimator(block))

    fun then(chainedAnimator: ChainedAnimator) : ChainedAnimator {
        this.next = chainedAnimator
        chainedAnimator.prev = this
        return chainedAnimator
    }

    fun withEndAction(block: () -> Unit): ChainedAnimator {
        this.block = block
        return this
    }

    fun start(): ChainedAnimator {
        prev?.start() ?: c.invoke().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    block?.invoke()
                    next?.prev = null
                    next?.start()
                }
            })
            start()
        }
        return this
    }
}