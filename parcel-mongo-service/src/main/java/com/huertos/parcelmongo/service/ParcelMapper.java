package com.huertos.parcelmongo.service;

import com.huertos.parcelmongo.dto.GeoLocationResponse;
import com.huertos.parcelmongo.dto.ParcelResponse;
import com.huertos.parcelmongo.model.GeoLocation;
import com.huertos.parcelmongo.model.Parcel;

public final class ParcelMapper {

    private ParcelMapper() {
    }

    public static ParcelResponse toResponse(Parcel parcel) {
        GeoLocation location = parcel.getLocation();
        return new ParcelResponse(
                parcel.getId(),
                parcel.getName(),
                new GeoLocationResponse(location.getLatitude(), location.getLongitude()),
                parcel.getSoilType(),
                parcel.getOwnerOrLead(),
                parcel.getStatus(),
                parcel.getCreatedAt()
        );
    }
}
