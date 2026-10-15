package com.huertos.parcelpostgres.repository;

import com.huertos.parcelpostgres.model.GeoLocation;
import com.huertos.parcelpostgres.model.Parcel;
import com.huertos.parcelpostgres.model.ParcelStatus;
import com.huertos.parcelpostgres.model.SoilType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ParcelRepositoryTest {

    @Autowired
    private ParcelRepository parcelRepository;

    @Test
    void savesParcelAndMatchesNameIgnoringCase() {
        Parcel parcel = new Parcel();
        parcel.setName("Huerto Norte");
        parcel.setLocation(new GeoLocation(19.4326, -99.1332));
        parcel.setSoilType(SoilType.LIMOSO);
        parcel.setOwnerOrLead("Ana Ruiz");
        parcel.setStatus(ParcelStatus.ACTIVA);
        parcel.setCreatedAt(Instant.parse("2026-10-14T18:00:00Z"));

        Parcel saved = parcelRepository.saveAndFlush(parcel);

        assertThat(saved.getId()).isNotNull();
        assertThat(parcelRepository.existsByNameIgnoreCase("huerto norte")).isTrue();
        assertThat(parcelRepository.findById(saved.getId())).isPresent();
        assertThat(parcelRepository.findAllByOrderByCreatedAtDesc()).hasSize(1);
    }
}
