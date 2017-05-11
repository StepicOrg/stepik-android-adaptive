package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.Attempt;

public final class AttemptRequest {
    private Attempt attempt;

    public AttemptRequest(final long step) {
        this.attempt = new Attempt(step);
    }
}
