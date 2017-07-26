package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.View;

public final class ViewRequest {
    private View view;

    public ViewRequest(long assignment, long step) {
        this.view = new View(assignment, step);
    }
}
