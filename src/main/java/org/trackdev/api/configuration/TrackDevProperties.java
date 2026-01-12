package org.trackdev.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "trackdev")
public class TrackDevProperties {

    private final Auth auth = new Auth();
    private final Admin admin = new Admin();

    public Auth getAuth() {
        return auth;
    }

    public Admin getAdmin() {
        return admin;
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

    public static class Admin {
        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
