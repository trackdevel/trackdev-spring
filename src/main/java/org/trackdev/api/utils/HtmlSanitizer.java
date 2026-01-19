package org.trackdev.api.utils;

/**
 * Utility class for sanitizing HTML content to prevent XSS attacks.
 * Escapes potentially dangerous HTML characters in user-provided content.
 */
public final class HtmlSanitizer {

    private HtmlSanitizer() {
        // Utility class - prevent instantiation
    }

    /**
     * Sanitizes a string by escaping HTML special characters.
     * This prevents XSS attacks by converting characters like < > " ' & 
     * into their HTML entity equivalents.
     *
     * @param input The input string to sanitize
     * @return The sanitized string with HTML characters escaped, or null if input is null
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        
        StringBuilder sanitized = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<':
                    sanitized.append("&lt;");
                    break;
                case '>':
                    sanitized.append("&gt;");
                    break;
                case '"':
                    sanitized.append("&quot;");
                    break;
                case '\'':
                    sanitized.append("&#x27;");
                    break;
                case '&':
                    sanitized.append("&amp;");
                    break;
                case '/':
                    sanitized.append("&#x2F;");
                    break;
                default:
                    sanitized.append(c);
            }
        }
        return sanitized.toString();
    }

    /**
     * Checks if a string contains potentially dangerous HTML content.
     * Useful for validation without modification.
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
