package com.huertos.parcelmongo.service;

import com.huertos.parcelmongo.dto.ParcelRequest;
import com.huertos.parcelmongo.dto.ParcelResponse;

import java.util.List;

public interface ParcelService {

    ParcelResponse create(ParcelRequest request);

    List<ParcelResponse> findAll();

    ParcelResponse findById(String id);
}
