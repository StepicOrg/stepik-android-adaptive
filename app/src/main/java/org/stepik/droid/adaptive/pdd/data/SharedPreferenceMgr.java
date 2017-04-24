package org.stepik.droid.adaptive.pdd.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.stepik.droid.adaptive.pdd.api.oauth.OAuthResponse;
import org.stepik.droid.adaptive.pdd.data.model.Profile;

public final class SharedPreferenceMgr {
    private static final String OAUTH_RESPONSE = "oauth_response";
    public static final String OAUTH_RESPONSE_DEADLINE = "oauth_response_deadline";

    private static final String PROFILE = "profile";
    public static final String PROFILE_ID = "profile_id";


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

        saveString(OAUTH_RESPONSE, json);
        saveLong(OAUTH_RESPONSE_DEADLINE, System.currentTimeMillis() + response.getExpiresIn() * 1000);
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


    public void saveBoolean(String name, Boolean data){
        sharedPreferences.edit().putBoolean(name, data).apply();
    }

    public void saveString(final String name, final String data){
        sharedPreferences.edit().putString(name, data).apply();
    }

    public void saveInt(final String name, final int data){
        sharedPreferences.edit().putInt(name, data).apply();
    }

    public void saveLong(final String name, final long data){
        sharedPreferences.edit().putLong(name, data).apply();
    }

    public String getString(final String name){
        return sharedPreferences.getString(name, null);
    }

    public int getInt(final String name){
        return sharedPreferences.getInt(name, 0);
    }

    public long getLong(final String name){
        return sharedPreferences.getLong(name, 0);
    }

    public boolean getBoolean(final String name){
        return sharedPreferences.getBoolean(name, false);
    }

    public void clear(){
        sharedPreferences.edit().clear().apply();
    }
    public void remove(final String name){
        sharedPreferences.edit().remove(name).apply();
    }

}