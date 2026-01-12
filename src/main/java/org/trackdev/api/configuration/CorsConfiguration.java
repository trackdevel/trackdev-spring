package org.trackdev.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("trackdev.frontend")
public class CorsConfiguration {

    private String url;

    public boolean isEnabled() {
        return url != null && !url.equals("");
    }

    public String getAllowedOrigin() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isAllowedOrigin(String origin) {
        return this.isEnabled() && origin != null && origin.equals(this.url);
    }
}
