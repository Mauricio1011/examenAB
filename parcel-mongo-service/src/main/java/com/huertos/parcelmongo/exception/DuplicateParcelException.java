package com.huertos.parcelmongo.exception;

public class DuplicateParcelException extends RuntimeException {

    public DuplicateParcelException(String name) {
        super(ErrorMessages.PARCEL_DUPLICATE.formatted(name));
    }
}
