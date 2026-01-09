package org.trackdev.api.configuration;

/**
 * User type enumeration with i18n support.
 * The message key is used to look up translations from message properties files.
 */
public enum UserType {
    ADMIN("user.type.admin"),
    PROFESSOR("user.type.professor"),
    STUDENT("user.type.student");

    private final String messageKey;

    UserType(final String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Returns the message key for i18n lookup.
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @deprecated Use I18nService for i18n support.
     * This method returns the enum name for backwards compatibility.
     */
    @Deprecated
    @Override
    public String toString() {
        return name();
    }
}