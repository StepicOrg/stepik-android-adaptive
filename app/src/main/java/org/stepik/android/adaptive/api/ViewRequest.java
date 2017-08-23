package org.stepik.android.adaptive.api;

import org.stepik.android.adaptive.data.model.View;

public final class ViewRequest {
    private View view;

    public ViewRequest(long assignment, long step) {
        this.view = new View(assignment, step);
    }
}
