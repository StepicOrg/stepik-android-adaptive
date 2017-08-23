package org.stepik.android.adaptive.api;


import org.stepik.android.adaptive.data.model.Attempt;

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
