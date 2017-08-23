package org.stepik.android.adaptive.api;

import org.stepik.android.adaptive.data.model.Step;

import java.util.List;

public class StepsResponse {
    private List<Step> steps;

    public List<Step> getSteps() {
        return steps;
    }

    public Step getFirstStep() {
        if (steps != null && steps.size() > 0) {
            return steps.get(0);
        } else {
            return null;
        }
    }
}
