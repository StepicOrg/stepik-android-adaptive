package org.stepik.android.adaptive;


import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Config {
    private static Configuration instance;

    public synchronized static void init(final Context context) {
        if (instance == null) {
            try  {
                final InputStream is = context.getAssets().open("config.json");
                instance = new Gson().fromJson(new InputStreamReader(is), Configuration.class);
            } catch (final IOException e) {
                instance = new Configuration();
            }
        }
    }

    public synchronized static Configuration getInstance() {
        return instance;
    }

    public static class Configuration {
        private long COURSE_ID = 0;

        private String HOST = "HOST";
        private String RATING_HOST = "RATING_HOST";

        private String OAUTH_CLIENT_ID = "OAUTH_CLIENT_ID";
        private String OAUTH_CLIENT_SECRET = "OAUTH_CLIENT_SECRET";
        private String GRANT_TYPE = "GRANT_TYPE";

        private String OAUTH_CLIENT_ID_SOCIAL = "OAUTH_CLIENT_ID_SOCIAL";
        private String OAUTH_CLIENT_SECRET_SOCIAL = "OAUTH_CLIENT_SECRET_SOCIAL";
        private String GRANT_TYPE_SOCIAL = "GRANT_TYPE_SOCIAL";

        private String REDIRECT_URI = "REDIRECT_URI";

        private String REFRESH_GRANT_TYPE = "REFRESH_GRANT_TYPE";

        private String GOOGLE_SERVER_CLIENT_ID = "GOOGLE_SERVER_CLIENT_ID";

        private String CODE_QUERY_PARAMETER = "CODE_QUERY_PARAMETER";

        private String APP_PUBLIC_LICENSE_KEY = "APP_PUBLIC_LICENSE_KEY";

        public String getHost() {
            return HOST;
        }

        public String getRatingHost() {
            return RATING_HOST;
        }

        public long getCourseId() {
            return COURSE_ID;
        }

        public String getOAuthClientId() {
            return OAUTH_CLIENT_ID;
        }

        public String getOAuthClientSecret() {
            return OAUTH_CLIENT_SECRET;
        }

        public String getGrantType() {
            return GRANT_TYPE;
        }

        public String getOAuthClientIdSocial() {
            return OAUTH_CLIENT_ID_SOCIAL;
        }

        public String getOAuthClientSecretSocial() {
            return OAUTH_CLIENT_SECRET_SOCIAL;
        }

        public String getGrantTypeSocial() {
            return GRANT_TYPE_SOCIAL;
        }

        public String getRedirectUri() {
            return REDIRECT_URI;
        }

        public String getRefreshGrantType() {
            return REFRESH_GRANT_TYPE;
        }

        public String getGoogleServerClientId() {
            return GOOGLE_SERVER_CLIENT_ID;
        }

        public String getCodeQueryParameter() {
            return CODE_QUERY_PARAMETER;
        }

        public String getAppPublicLicenseKey() {
            return APP_PUBLIC_LICENSE_KEY;
        }
    }
}
