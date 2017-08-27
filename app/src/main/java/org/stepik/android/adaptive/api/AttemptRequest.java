package org.stepik.android.adaptive.api;

import org.stepik.android.adaptive.data.model.Attempt;

public final class AttemptRequest {
    private Attempt attempt;

    public AttemptRequest(final long step) {
        this.attempt = new Attempt(step);
    }
}
