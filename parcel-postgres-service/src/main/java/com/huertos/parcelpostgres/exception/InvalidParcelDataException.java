package com.huertos.parcelpostgres.exception;

import com.huertos.parcelpostgres.dto.FieldErrorDetail;

import java.util.List;

public class InvalidParcelDataException extends RuntimeException {

    private final List<FieldErrorDetail> fieldErrors;

    public InvalidParcelDataException(String message) {
        this(message, List.of());
    }

    public InvalidParcelDataException(String message, List<FieldErrorDetail> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public List<FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }
}
