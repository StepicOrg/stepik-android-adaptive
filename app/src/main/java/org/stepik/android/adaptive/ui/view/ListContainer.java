package org.stepik.android.adaptive.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.adaptive.ui.view.container.ContainerAdapter;
import org.stepik.android.adaptive.ui.view.container.ContainerView;

import java.util.ArrayList;
import java.util.List;

public class ListContainer extends LinearLayout implements ContainerView {
    public ListContainer(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public ListContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public ListContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    private List<ViewHolder> holders = new ArrayList<>();
    private ContainerAdapter adapter;

    @Override
    public void setAdapter(@NotNull ContainerAdapter adapter) {
        this.adapter = adapter;
        this.adapter.setContainer(this);
        holders.clear();
        for (int i = 0; i < adapter.getItemCount(); i++)
            holders.add(adapter.onCreateViewHolder(this));
        onDataSetChanged();
    }

    public ContainerAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onDataSetChanged() {
        removeAllViews();
        onRebind();
    }

    @Override
    public void onDataAdded() {
        onRebind();
    }

    @Override
    public void onRebind() {
        for (int i = 0; i < holders.size(); i++)
            onRebind(i);
    }

    @Override
    public void onRebind(int pos) {
        if (adapter != null) {
            adapter.onBindViewHolder(holders.get(pos), pos);
            if (!holders.get(pos).isAttached()) {
                holders.get(pos).setAttached(true);
                addView(holders.get(pos).getView(), pos);
            }
        }
    }
}
