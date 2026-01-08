package org.trackdev.api.entity;

/**
 * Sprint status enumeration with i18n support.
 * The message key is used to look up translations from message properties files.
 */
public enum SprintStatus {
    DRAFT("sprint.status.draft"),
    ACTIVE("sprint.status.active"),
    CLOSED("sprint.status.closed");

    private final String messageKey;

    SprintStatus(final String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Returns the message key for i18n lookup.
     * Use MessageSource.getMessage(getMessageKey(), null, locale) to get translated text.
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @deprecated Use I18nService.getLocalizedName() for i18n support.
     * This method returns the enum name for backwards compatibility.
     */
    @Deprecated
    @Override
    public String toString() {
        return name();
    }
}