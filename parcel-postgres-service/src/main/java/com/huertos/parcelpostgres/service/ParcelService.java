package com.huertos.parcelpostgres.service;

import com.huertos.parcelpostgres.dto.ParcelRequest;
import com.huertos.parcelpostgres.dto.ParcelResponse;

import java.util.List;

public interface ParcelService {

    ParcelResponse create(ParcelRequest request);

    List<ParcelResponse> findAll();

    ParcelResponse findById(String id);
}
