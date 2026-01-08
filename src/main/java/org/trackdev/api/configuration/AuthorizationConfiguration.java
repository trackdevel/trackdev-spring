package org.trackdev.api.configuration;

import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@ConfigurationProperties("trackdev.auth")
public class AuthorizationConfiguration {

    public static final int DEFAULT_TOKEN_LIFETIME_IN_MINUTES = 60*24;

    private Integer tokenLifetimeInMinutes;
    private SecretKey key;

    public int getTokenLifetimeInMinutes() {
        int value = DEFAULT_TOKEN_LIFETIME_IN_MINUTES;
        if(tokenLifetimeInMinutes != null) {
            value = tokenLifetimeInMinutes;
        }
        return value;
    }

    public void setTokenLifetimeInMinutes(Integer expirationInMinutes) {
        this.tokenLifetimeInMinutes = expirationInMinutes;
    }

    public void setSecretKeyBase(String base) {
        if (base != null) {
            this.key = Keys.hmacShaKeyFor(base.getBytes());
        }
    }

    public SecretKey getKey() {
        return this.key;
    }
}
