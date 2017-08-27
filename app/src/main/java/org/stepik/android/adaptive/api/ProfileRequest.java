package org.stepik.android.adaptive.api;

import org.stepik.android.adaptive.data.model.Profile;

public class ProfileRequest {
    public ProfileRequest(Profile profile) {
        this.profile = profile;
    }

    private Profile profile;
}
