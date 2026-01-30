package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.trackdev.api.entity.SprintStatus;
import org.trackdev.api.entity.TaskStatus;
import org.trackdev.api.entity.TaskType;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Service for handling i18n translations of TaskStatus, TaskType, and SprintStatus enums.
 */
@Service
public class I18nService {

    private final MessageSource messageSource;

    @Autowired
    public I18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // ========== TaskStatus methods ==========

    /**
     * Get localized name for a TaskStatus using the current locale.
     */
    public String getLocalizedName(TaskStatus status) {
        return getLocalizedName(status, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized name for a TaskStatus using a specific locale.
     */
    public String getLocalizedName(TaskStatus status, Locale locale) {
        return messageSource.getMessage(status.getMessageKey(), null, status.name(), locale);
    }

    /**
     * Get a map of all TaskStatus values with localized names.
     */
    public Map<String, String> getAllTaskStatusLocalized() {
        return getAllTaskStatusLocalized(LocaleContextHolder.getLocale());
    }

    /**
     * Get a map of all TaskStatus values with localized names for a specific locale.
     */
    public Map<String, String> getAllTaskStatusLocalized(Locale locale) {
        Map<String, String> statusMap = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusMap.put(status.name(), getLocalizedName(status, locale));
        }
        return statusMap;
    }

    /**
     * Get a map of User Story status values (BACKLOG to DONE) with localized names.
     */
    public Map<String, String> getUsStatusLocalized() {
        return getUsStatusLocalized(LocaleContextHolder.getLocale());
    }

    /**
     * Get a map of User Story status values (BACKLOG to DONE) with localized names for a specific locale.
     */
    public Map<String, String> getUsStatusLocalized(Locale locale) {
        Map<String, String> statusMap = new LinkedHashMap<>();
        int startOrdinal = TaskStatus.BACKLOG.ordinal();
        int endOrdinal = TaskStatus.DONE.ordinal();
        for (int i = startOrdinal; i <= endOrdinal; i++) {
            TaskStatus status = TaskStatus.values()[i];
            statusMap.put(status.name(), getLocalizedName(status, locale));
        }
        return statusMap;
    }

    /**
     * Get a map of subtask status values (TODO, INPROGRESS, VERIFY, DONE) with localized names.
     */
    public Map<String, String> getSubtaskStatusLocalized() {
        return getSubtaskStatusLocalized(LocaleContextHolder.getLocale());
    }

    /**
     * Get a map of subtask status values (TODO, INPROGRESS, VERIFY, DONE) with localized names for a specific locale.
     */
    public Map<String, String> getSubtaskStatusLocalized(Locale locale) {
        Map<String, String> statusMap = new LinkedHashMap<>();
        statusMap.put(TaskStatus.TODO.name(), getLocalizedName(TaskStatus.TODO, locale));
        statusMap.put(TaskStatus.INPROGRESS.name(), getLocalizedName(TaskStatus.INPROGRESS, locale));
        statusMap.put(TaskStatus.VERIFY.name(), getLocalizedName(TaskStatus.VERIFY, locale));
        statusMap.put(TaskStatus.DONE.name(), getLocalizedName(TaskStatus.DONE, locale));
        return statusMap;
    }

    // ========== TaskType methods ==========

    /**
     * Get localized name for a TaskType using the current locale.
     */
    public String getLocalizedName(TaskType type) {
        return getLocalizedName(type, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized name for a TaskType using a specific locale.
     */
    public String getLocalizedName(TaskType type, Locale locale) {
        return messageSource.getMessage(type.getMessageKey(), null, type.name(), locale);
    }

    /**
     * Get a map of all TaskType values with localized names.
     */
    public Map<String, String> getAllTaskTypesLocalized() {
        return getAllTaskTypesLocalized(LocaleContextHolder.getLocale());
    }

    /**
     * Get a map of all TaskType values with localized names for a specific locale.
     */
    public Map<String, String> getAllTaskTypesLocalized(Locale locale) {
        Map<String, String> typeMap = new LinkedHashMap<>();
        for (TaskType type : TaskType.values()) {
            typeMap.put(type.name(), getLocalizedName(type, locale));
        }
        return typeMap;
    }

    // ========== SprintStatus methods ==========

    /**
     * Get localized name for a SprintStatus using the current locale.
     */
    public String getLocalizedName(SprintStatus status) {
        return getLocalizedName(status, LocaleContextHolder.getLocale());
    }

    /**
     * Get localized name for a SprintStatus using a specific locale.
     */
    public String getLocalizedName(SprintStatus status, Locale locale) {
        return messageSource.getMessage(status.getMessageKey(), null, status.name(), locale);
    }

    /**
     * Get a map of all SprintStatus values with localized names.
     */
    public Map<String, String> getAllSprintStatusLocalized() {
        return getAllSprintStatusLocalized(LocaleContextHolder.getLocale());
    }

    /**
     * Get a map of all SprintStatus values with localized names for a specific locale.
     */
    public Map<String, String> getAllSprintStatusLocalized(Locale locale) {
        Map<String, String> statusMap = new LinkedHashMap<>();
        for (SprintStatus status : SprintStatus.values()) {
            statusMap.put(status.name(), getLocalizedName(status, locale));
        }
        return statusMap;
    }
}
