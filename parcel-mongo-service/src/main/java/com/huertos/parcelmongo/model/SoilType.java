package com.huertos.parcelmongo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.huertos.parcelmongo.exception.ErrorMessages;
import com.huertos.parcelmongo.sanitization.TextSanitizer;

import java.util.Locale;

public enum SoilType {
    ARCILLOSO,
    LIMOSO,
    ARENOSO;

    @JsonCreator
    public static SoilType fromJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = TextSanitizer.normalize(value);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }
        try {
            return SoilType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(ErrorMessages.SOIL_TYPE_INVALID);
        }
    }
}
