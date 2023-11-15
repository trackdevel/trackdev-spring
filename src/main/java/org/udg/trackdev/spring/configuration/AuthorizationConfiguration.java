package org.udg.trackdev.spring.configuration;

import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@ConfigurationProperties("trackdev.auth")
public class AuthorizationConfiguration {

    public static final int DEFAULT_TOKEN_LIFETIME_IN_MINUTES = 60*24;

    private Integer tokenLifetimeInMinutes;
    private Key key;

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

    public Key getKey() {
        return this.key;
    }
}
