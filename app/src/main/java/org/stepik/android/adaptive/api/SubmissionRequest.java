package org.stepik.android.adaptive.api;

import org.stepik.android.adaptive.data.model.Submission;

public final class SubmissionRequest {
    private final Submission submission;

    public SubmissionRequest(final Submission submission) {
        this.submission = submission;
    }
}
