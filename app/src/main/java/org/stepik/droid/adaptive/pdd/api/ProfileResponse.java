package org.stepik.droid.adaptive.pdd.api;

import org.stepik.droid.adaptive.pdd.data.model.Profile;
import org.stepik.droid.adaptive.pdd.data.model.User;

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
