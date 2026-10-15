package com.huertos.parcelmongo.controller;

import com.huertos.parcelmongo.dto.ParcelRequest;
import com.huertos.parcelmongo.dto.ParcelResponse;
import com.huertos.parcelmongo.service.ParcelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/parcels")
@Validated
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody ParcelRequest request) {
        ParcelResponse created = parcelService.create(request);
        URI location = URI.create("/api/v1/parcels/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ParcelResponse>> list() {
        return ResponseEntity.ok(parcelService.findAll());
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ParcelResponse> getById(
            @PathVariable @NotBlank(message = "{parcel.id.notBlank}") String id) {
        return ResponseEntity.ok(parcelService.findById(id));
    }
}
