package com.huertos.parcelmongo.validation;

import com.huertos.parcelmongo.sanitization.TextSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedTextValidator implements ConstraintValidator<AllowedText, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return TextSanitizer.isAllowedText(value);
    }
}
