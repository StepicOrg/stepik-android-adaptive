package org.stepik.droid.adaptive.pdd.api;

import org.stepik.droid.adaptive.pdd.data.model.Submission;

public final class SubmissionRequest {
    private final Submission submission;

    public SubmissionRequest(final Submission submission) {
        this.submission = submission;
    }
}
