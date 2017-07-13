package org.stepik.android.adaptive.pdd.ui.view.container

import android.view.ViewGroup

abstract class ContainerAdapter<VH : ContainerView.ViewHolder> {
    public var container: ContainerView? = null
        set

    protected final fun onDataSetChanged() {
        container?.onDataSetChanged()
    }

    protected final fun onDataAdded() {
        container?.onDataAdded()
    }

    protected final fun onRebind() {
        container?.onRebind()
    }

    protected final fun onRebind(pos: Int) {
        container?.onRebind(pos)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup) : VH
    abstract fun getItemCount() : Int
    abstract fun onBindViewHolder(holder: VH, pos: Int)
}