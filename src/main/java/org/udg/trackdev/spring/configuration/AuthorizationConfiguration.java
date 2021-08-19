package org.udg.trackdev.spring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("trackdev.auth")
public class AuthorizationConfiguration {

    public static final int DEFAULT_TOKEN_LIFETIME_IN_MINUTES = 10;

    private Integer tokenLifetimeInMinutes;

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
}
