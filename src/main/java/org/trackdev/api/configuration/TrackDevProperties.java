package org.trackdev.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "trackdev")
public class TrackDevProperties {

    private final Cors cors = new Cors();
    private final Auth auth = new Auth();

    public Cors getCors() {
        return cors;
    }

    public Auth getAuth() {
        return auth;
    }

    public static class Cors {
        private String allowedOrigin;

        public String getAllowedOrigin() {
            return allowedOrigin;
        }

        public void setAllowedOrigin(String allowedOrigin) {
            this.allowedOrigin = allowedOrigin;
        }
    }

    public static class Auth {
        private int tokenLifetimeInMinutes;
        private String secretKeyBase;

        public int getTokenLifetimeInMinutes() {
            return tokenLifetimeInMinutes;
        }

        public void setTokenLifetimeInMinutes(int tokenLifetimeInMinutes) {
            this.tokenLifetimeInMinutes = tokenLifetimeInMinutes;
        }

        public String getSecretKeyBase() {
            return secretKeyBase;
        }

        public void setSecretKeyBase(String secretKeyBase) {
            this.secretKeyBase = secretKeyBase;
        }
    }
}
