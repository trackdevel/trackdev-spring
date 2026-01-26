package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for resolving i18n message keys to localized strings.
 * Uses the Accept-Language header from the request to determine the locale.
 */
@Service
public class MessageResolver {

    @Autowired
    private MessageSource messageSource;

    /**
     * Resolves a message key to a localized string using the current request locale.
     * If the key is not found, returns the key itself (for backward compatibility
     * with hardcoded messages during migration).
     *
     * @param messageKey The message key to resolve (e.g., "error.user.not.found")
     * @param args Optional arguments for message placeholders ({0}, {1}, etc.)
     * @return The localized message string
     */
    public String getMessage(String messageKey, Object... args) {
        if (messageKey == null || messageKey.isEmpty()) {
            return messageKey;
        }
        
        Locale locale = LocaleContextHolder.getLocale();
        
        try {
            return messageSource.getMessage(messageKey, args, locale);
        } catch (Exception e) {
            // If the key is not found, check if it looks like a message key
            // If not, return it as-is (backward compatibility for hardcoded messages)
            if (messageKey.startsWith("error.")) {
                // It's a message key but not found - return the key for debugging
                return messageKey;
            }
            // It's likely a hardcoded message, return as-is
            return messageKey;
        }
    }

    /**
     * Resolves a message key to a localized string using a specific locale.
     *
     * @param messageKey The message key to resolve
     * @param locale The locale to use for resolution
     * @param args Optional arguments for message placeholders
     * @return The localized message string
     */
    public String getMessage(String messageKey, Locale locale, Object... args) {
        if (messageKey == null || messageKey.isEmpty()) {
            return messageKey;
        }
        
        try {
            return messageSource.getMessage(messageKey, args, locale);
        } catch (Exception e) {
            if (messageKey.startsWith("error.")) {
                return messageKey;
            }
            return messageKey;
        }
    }

    /**
     * Gets the current locale from the request context.
     *
     * @return The current locale
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }
}
