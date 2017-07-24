package org.stepik.android.adaptive.pdd.api;

import org.stepik.android.adaptive.pdd.data.model.Profile;

public class ProfileRequest {
    public ProfileRequest(Profile profile) {
        this.profile = profile;
    }

    private Profile profile;
}
