package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.Config;
import org.stepik.android.adaptive.pdd.data.SharedPreferenceMgr;
import org.stepik.android.adaptive.pdd.data.model.Submission;
import org.stepik.android.adaptive.pdd.util.ExpUtil;

public final class SubmissionRequest {
    private final Submission submission;
    private final long course = Config.getInstance().getCourseId();
    private final long user = SharedPreferenceMgr.getInstance().getProfileId();
    private final boolean streak_restored = ExpUtil.getStreak() > 2;

    public SubmissionRequest(final Submission submission) {
        this.submission = submission;
    }
}
