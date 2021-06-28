package org.udg.trackdev.spring.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("trackdev.cors")
public class CorsConfiguration {

    private String allowedOrigin;

    public boolean isEnabled() {
        return allowedOrigin != null && !allowedOrigin.equals("");
    }

    public String getAllowedOrigin() {
        return allowedOrigin;
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    public boolean isAllowedOrigin(String origin) {
        return this.isEnabled() && origin.equals(this.allowedOrigin);
    }
}
