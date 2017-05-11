package org.stepik.android.adaptive.pdd.api.login;


public class SocialManager {
    private static final String GOOGLE_SOCIAL_IDENTIFIER = "google";
    private static final String FACEBOOK_SOCIAL_IDENTIFIER = "facebook";
    private static final String MAILRU_SOCIAL_IDENTIFIER = "itmailru";
    private static final String TWITTER_SOCIAL_IDENTIFIER = "twitter";
    private static final String GITHUB_SOCIAL_IDENTIFIER = "github";
    private static final String VK_SOCIAL_IDENTIFIER = "vk";

    public enum SocialType {
        vk(VK_SOCIAL_IDENTIFIER),
        google(GOOGLE_SOCIAL_IDENTIFIER);

        private final String identifier;

        SocialType(final String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        public boolean needUseAccessTokenInsteadOfCode() {
            return identifier.equals(VK_SOCIAL_IDENTIFIER) || identifier.equals(FACEBOOK_SOCIAL_IDENTIFIER);
        }
    }
}
