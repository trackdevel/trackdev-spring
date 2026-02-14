package org.trackdev.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "trackdev")
public class TrackDevProperties {

    private final Auth auth = new Auth();
    private final Admin admin = new Admin();
    private final Mail mail = new Mail();
    private final Cors cors = new Cors();
    private final Frontend frontend = new Frontend();
    private final Discord discord = new Discord();

    public Auth getAuth() {
        return auth;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Mail getMail() {
        return mail;
    }

    public Cors getCors() {
        return cors;
    }

    public Frontend getFrontend() {
        return frontend;
    }

    public Discord getDiscord() {
        return discord;
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

        @Override
        public String toString() {
            return "Auth{tokenLifetimeInMinutes=" + tokenLifetimeInMinutes +
                    ", secretKeyBase='***'}";
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

        @Override
        public String toString() {
            return "Admin{username='" + username + "', email='" + email + "', password='***'}";
        }
    }

    public static class Mail {
        private String host;
        private int port;
        private String username;
        private String password;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        @Override
        public String toString() {
            return "Mail{host='" + host + "', port=" + port + 
                   ", username='" + username + "', password='***'}";
        }
    }

    public static class Cors {
        private String allowedOrigin;

        public String getAllowedOrigin() { return allowedOrigin; }
        public void setAllowedOrigin(String allowedOrigin) { this.allowedOrigin = allowedOrigin; }

        @Override
        public String toString() {
            return "Cors{allowedOrigin='" + allowedOrigin + "'}";
        }
    }

    public static class Frontend {
        private String url;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        @Override
        public String toString() {
            return "Frontend{url='" + url + "'}";
        }
    }

    public static class Discord {
        private String clientId;
        private String clientSecret;
        private String botToken;
        private String guildId;
        private String verifiedRoleId;
        private String redirectUri;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getGuildId() {
            return guildId;
        }

        public void setGuildId(String guildId) {
            this.guildId = guildId;
        }

        public String getVerifiedRoleId() {
            return verifiedRoleId;
        }

        public void setVerifiedRoleId(String verifiedRoleId) {
            this.verifiedRoleId = verifiedRoleId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        @Override
        public String toString() {
            return "Discord{clientId='" + clientId + "', guildId='" + guildId +
                    "', verifiedRoleId='" + verifiedRoleId + "', redirectUri='" + redirectUri + "'}";
        }
    }

    @Override
    public String toString() {
        return "TrackDevProperties{" +
                "\n  auth=" + auth +
                ",\n  admin=" + admin +
                ",\n  mail=" + mail +
                ",\n  cors=" + cors +
                ",\n  frontend=" + frontend +
                ",\n  discord=" + discord +
                "\n}";
    }
}
