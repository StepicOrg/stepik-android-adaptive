package org.stepik.android.adaptive.pdd.api;


import org.stepik.android.adaptive.pdd.data.model.Attempt;

import java.util.List;

public class AttemptResponse {
    private List<Attempt> attempts;

    public List<Attempt> getAttempts() {
        return attempts;
    }

    public Attempt getFirstAttempt() {
        return attempts != null && attempts.size() > 0 ? attempts.get(0) : null;
    }
}
