package org.stepik.android.adaptive.api;

import org.stepik.android.adaptive.data.model.Profile;
import org.stepik.android.adaptive.data.model.User;

import java.util.List;

public final class ProfileResponse {
    private List<User> users;
    private List<Profile> profiles;

    public Profile getProfile() {
        if (profiles == null || profiles.size() < 1)
            return null;
        else
            return profiles.get(0);
    }
}
