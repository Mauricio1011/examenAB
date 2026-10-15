package com.huertos.parcelmongo.dto;

import com.huertos.parcelmongo.model.SoilType;
import com.huertos.parcelmongo.sanitization.TextSanitizer;
import com.huertos.parcelmongo.validation.AllowedText;
import com.huertos.parcelmongo.validation.NoMaliciousContent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParcelRequest(
        @NotBlank(message = "{parcel.name.notBlank}")
        @Size(min = 3, max = 100, message = "{parcel.name.size}")
        @AllowedText
        @NoMaliciousContent
        String name,
        @NotNull(message = "{parcel.location.notNull}")
        @Valid
        GeoLocationRequest location,
        @NotNull(message = "{parcel.soilType.notNull}")
        SoilType soilType,
        @NotBlank(message = "{parcel.owner.notBlank}")
        @Size(min = 3, max = 120, message = "{parcel.owner.size}")
        @AllowedText
        @NoMaliciousContent
        String ownerOrLead
) {

    public ParcelRequest {
        name = TextSanitizer.normalize(name);
        ownerOrLead = TextSanitizer.normalize(ownerOrLead);
    }
}
