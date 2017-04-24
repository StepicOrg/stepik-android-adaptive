package org.stepik.droid.adaptive.pdd.api;

import org.stepik.droid.adaptive.pdd.data.model.Submission;

import java.util.List;

public class SubmissionResponse {
    private List<Submission> submissions;

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public Submission getFirstSubmission() {
        return submissions != null && submissions.size() > 0 ? submissions.get(0) : null;
    }
}
