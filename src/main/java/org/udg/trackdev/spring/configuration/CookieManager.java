package org.udg.trackdev.spring.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CookieManager {

    @Autowired
    CorsConfiguration corsConfiguration;

    public void addSessionCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue) {
        String sameSite = "Lax";
        String requestOrigin = request.getHeader("Origin");
        if(corsConfiguration.isEnabled() && corsConfiguration.isAllowedOrigin(requestOrigin)) {
            sameSite = "None";
        }
        ResponseCookie cookie = ResponseCookie.from(cookieName, cookieValue)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite(sameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void removeCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
