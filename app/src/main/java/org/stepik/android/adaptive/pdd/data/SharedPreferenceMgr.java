package org.stepik.android.adaptive.pdd.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.stepik.android.adaptive.pdd.api.API;
import org.stepik.android.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.android.adaptive.pdd.data.model.Profile;

public final class SharedPreferenceMgr {
    private static final String OAUTH_RESPONSE = "oauth_response";
    private static final String IS_OAUTH_TOKEN_SOCIAL = "is_oauth_token_social";
    private static final String OAUTH_RESPONSE_DEADLINE = "oauth_response_deadline";

    private static final String PROFILE = "profile";
    private static final String PROFILE_ID = "profile_id";

    private static final String NOT_FIRST_TIME = "not_first_time";

    private static SharedPreferenceMgr instance;

    private final SharedPreferences sharedPreferences;

    private SharedPreferenceMgr(final Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public synchronized static void init(final Context context) {
        if (instance == null) {
            instance = new SharedPreferenceMgr(context);
        }
    }

    public synchronized static SharedPreferenceMgr getInstance() {
        return instance;
    }


    public void saveOAuthResponse(final OAuthResponse response) {
        final Gson gson = new Gson();
        final String json = gson.toJson(response);

        final long currentTime = DateTime.now(DateTimeZone.UTC).getMillis();

        saveString(OAUTH_RESPONSE, json);
        saveLong(OAUTH_RESPONSE_DEADLINE, currentTime + (response.getExpiresIn() - 50) * 1000);
    }

    public OAuthResponse getOAuthResponse() {
        final String json = getString(OAUTH_RESPONSE);
        if (json == null) return null;

        final Gson gson = new Gson();
        return gson.fromJson(json, OAuthResponse.class);
    }

    public void saveProfile(final Profile profile) {
        final Gson gson = new Gson();
        final String json = gson.toJson(profile);

        saveString(PROFILE, json);
        saveLong(PROFILE_ID, profile.getId());
    }

    public Profile getProfile() {
        final String json = getString(PROFILE);
        if (json == null) return null;

        final Gson gson = new Gson();
        return gson.fromJson(json, Profile.class);
    }

    public void removeProfile() {
        API.authLock.lock();
        remove(PROFILE);
        remove(PROFILE_ID);
        remove(OAUTH_RESPONSE);
        remove(IS_OAUTH_TOKEN_SOCIAL);
        remove(OAUTH_RESPONSE_DEADLINE);
        API.authLock.unlock();
    }

    public void setNotFirstTime(final boolean notFirstTime) {
        saveBoolean(NOT_FIRST_TIME, notFirstTime);
    }

    public boolean isNotFirstTime() {
        return getBoolean(NOT_FIRST_TIME);
    }

    public void setIsOauthTokenSocial(final boolean isOauthTokenSocial) {
        saveBoolean(IS_OAUTH_TOKEN_SOCIAL, isOauthTokenSocial);
    }

    public boolean isAuthTokenSocial() {
        return getBoolean(IS_OAUTH_TOKEN_SOCIAL);
    }

    public long getAuthResponseDeadline() {
        return getLong(OAUTH_RESPONSE_DEADLINE);
    }

    public long getProfileId() {
        return getLong(PROFILE_ID);
    }

    public void saveBoolean(String name, Boolean data) {
        sharedPreferences.edit().putBoolean(name, data).apply();
    }

    private void saveString(final String name, final String data) {
        sharedPreferences.edit().putString(name, data).apply();
    }

    public void saveLong(final String name, final long data) {
        sharedPreferences.edit().putLong(name, data).apply();
    }

    private String getString(final String name){
        return sharedPreferences.getString(name, null);
    }

    public long getLong(final String name){
        return sharedPreferences.getLong(name, 0);
    }

    public boolean getBoolean(final String name) {
        return sharedPreferences.getBoolean(name, false);
    }

    public void remove(final String name){
        sharedPreferences.edit().remove(name).apply();
    }

}