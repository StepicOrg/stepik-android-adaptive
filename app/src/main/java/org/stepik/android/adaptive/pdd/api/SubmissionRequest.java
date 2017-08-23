package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.Submission;

public final class SubmissionRequest {
    private final Submission submission;

    public SubmissionRequest(final Submission submission) {
        this.submission = submission;
    }
}
