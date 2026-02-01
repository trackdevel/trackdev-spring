package org.trackdev.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trackdev.api.service.EncryptionService;

/**
 * JPA AttributeConverter that transparently encrypts/decrypts string fields.
 * 
 * Usage: Add @Convert(converter = EncryptedStringConverter.class) to entity fields
 * that should be encrypted at rest.
 * 
 * Note: This converter is NOT auto-applied. You must explicitly annotate fields.
 */
@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(EncryptionService service) {
        EncryptedStringConverter.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        if (encryptionService == null) {
            // Encryption service not yet initialized (during startup)
            return attribute;
        }
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (encryptionService == null) {
            // Encryption service not yet initialized (during startup)
            return dbData;
        }
        return encryptionService.decrypt(dbData);
    }
}
