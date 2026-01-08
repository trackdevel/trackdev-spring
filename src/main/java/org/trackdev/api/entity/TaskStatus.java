package org.trackdev.api.entity;

/**
 * Task status enumeration with i18n support.
 * The message key is used to look up translations from message properties files.
 */
public enum TaskStatus {
    BACKLOG("task.status.backlog"),
    TODO("task.status.todo"),
    INPROGRESS("task.status.inprogress"),
    VERIFY("task.status.verify"),
    DONE("task.status.done"),
    DEFINED("task.status.defined");

    private final String messageKey;

    TaskStatus(final String messageKey) {
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
     * @deprecated Use TaskStatusService.getLocalizedName() for i18n support.
     * This method returns the enum name for backwards compatibility.
     */
    @Deprecated
    @Override
    public String toString() {
        return name();
    }
}