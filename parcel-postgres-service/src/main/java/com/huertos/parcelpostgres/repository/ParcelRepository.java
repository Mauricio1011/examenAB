package com.huertos.parcelpostgres.repository;

import com.huertos.parcelpostgres.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParcelRepository extends JpaRepository<Parcel, UUID> {

    boolean existsByNameIgnoreCase(String name);

    List<Parcel> findAllByOrderByCreatedAtDesc();
}
