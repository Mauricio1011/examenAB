package com.huertos.parcelmongo.repository;

import com.huertos.parcelmongo.model.Parcel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ParcelRepository extends MongoRepository<Parcel, String> {

    boolean existsByNameIgnoreCase(String name);

    List<Parcel> findAllByOrderByCreatedAtDesc();
}
