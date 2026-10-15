package com.huertos.parcelpostgres.service;

import com.huertos.parcelpostgres.dto.GeoLocationResponse;
import com.huertos.parcelpostgres.dto.ParcelResponse;
import com.huertos.parcelpostgres.model.GeoLocation;
import com.huertos.parcelpostgres.model.Parcel;

public final class ParcelMapper {

    private ParcelMapper() {
    }

    public static ParcelResponse toResponse(Parcel parcel) {
        GeoLocation location = parcel.getLocation();
        return new ParcelResponse(
                parcel.getId() == null ? null : parcel.getId().toString(),
                parcel.getName(),
                new GeoLocationResponse(location.getLatitude(), location.getLongitude()),
                parcel.getSoilType(),
                parcel.getOwnerOrLead(),
                parcel.getStatus(),
                parcel.getCreatedAt()
        );
    }
}
