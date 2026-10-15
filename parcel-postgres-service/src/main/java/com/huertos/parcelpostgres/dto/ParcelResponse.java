package com.huertos.parcelpostgres.dto;

import com.huertos.parcelpostgres.model.ParcelStatus;
import com.huertos.parcelpostgres.model.SoilType;

import java.time.Instant;

public record ParcelResponse(
        String id,
        String name,
        GeoLocationResponse location,
        SoilType soilType,
        String ownerOrLead,
        ParcelStatus status,
        Instant createdAt
) {
}
