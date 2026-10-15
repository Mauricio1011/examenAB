package com.huertos.parcelmongo.exception;

public class ParcelNotFoundException extends RuntimeException {

    public ParcelNotFoundException(String id) {
        super(ErrorMessages.PARCEL_NOT_FOUND.formatted(id));
    }
}
