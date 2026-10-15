package com.huertos.parcelmongo.dto;

import com.huertos.parcelmongo.model.ParcelStatus;
import com.huertos.parcelmongo.model.SoilType;

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
