package com.huertos.parcelpostgres.validation;

import com.huertos.parcelpostgres.sanitization.TextSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoMaliciousContentValidator implements ConstraintValidator<NoMaliciousContent, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !TextSanitizer.hasMaliciousContent(value);
    }
}
