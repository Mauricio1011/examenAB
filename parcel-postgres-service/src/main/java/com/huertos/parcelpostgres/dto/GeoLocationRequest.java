package com.huertos.parcelpostgres.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record GeoLocationRequest(
        @NotNull(message = "{parcel.latitude.notNull}")
        @DecimalMin(value = "-90.0", message = "{parcel.latitude.range}")
        @DecimalMax(value = "90.0", message = "{parcel.latitude.range}")
        Double latitude,
        @NotNull(message = "{parcel.longitude.notNull}")
        @DecimalMin(value = "-180.0", message = "{parcel.longitude.range}")
        @DecimalMax(value = "180.0", message = "{parcel.longitude.range}")
        Double longitude
) {
}
