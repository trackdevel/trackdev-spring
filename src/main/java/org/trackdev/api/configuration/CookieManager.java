package org.trackdev.api.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieManager {

    @Autowired
    CorsConfiguration corsConfiguration;

    public void addSessionCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue) {
        String sameSite = getSameSite(request);
        ResponseCookie cookie = ResponseCookie.from(cookieName, cookieValue)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        String sameSite = getSameSite(request);
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .path("/")
                .secure(true)
                .httpOnly(true)
                .maxAge(0)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String getSameSite(HttpServletRequest request) {
        String sameSite = "Lax";
        String requestOrigin = request.getHeader("Origin");
        if(corsConfiguration.isEnabled() && corsConfiguration.isAllowedOrigin(requestOrigin)) {
            sameSite = "None";
        }
        return sameSite;
    }
}
