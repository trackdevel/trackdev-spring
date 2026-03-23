package org.trackdev.api.utils;

import org.trackdev.api.controller.exceptions.EntityException;

/**
 * Utility class for validating user-provided text content to prevent XSS attacks.
 * Rejects input containing dangerous HTML patterns instead of escaping characters.
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates that a string does not contain dangerous HTML content.
     * Throws an exception if potentially dangerous patterns are detected.
     *
     * @param input The input string to validate
     * @throws EntityException if the input contains dangerous HTML patterns
     */
    public static void validate(String input) {
        if (input != null && containsHtml(input)) {
            throw new EntityException(ErrorConstants.INPUT_CONTAINS_HTML);
        }
    }

    /**
     * Checks if a string contains potentially dangerous HTML content.
     *
     * @param input The input string to check
     * @return true if the string contains HTML-like content
     */
    public static boolean containsHtml(String input) {
        if (input == null) {
            return false;
        }
        // Check for common XSS patterns
        String lower = input.toLowerCase();
        return lower.contains("<script") ||
               lower.contains("<img") ||
               lower.contains("javascript:") ||
               lower.contains("onerror") ||
               lower.contains("onload") ||
               lower.contains("onclick") ||
               lower.contains("<iframe") ||
               lower.contains("<object") ||
               lower.contains("<embed") ||
               lower.contains("<svg") ||
               lower.contains("expression(");
    }
}
