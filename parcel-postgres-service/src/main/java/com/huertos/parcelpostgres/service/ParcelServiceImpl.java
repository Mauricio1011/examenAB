package com.huertos.parcelpostgres.service;

import com.huertos.parcelpostgres.dto.FieldErrorDetail;
import com.huertos.parcelpostgres.dto.GeoLocationRequest;
import com.huertos.parcelpostgres.dto.ParcelRequest;
import com.huertos.parcelpostgres.dto.ParcelResponse;
import com.huertos.parcelpostgres.exception.DuplicateParcelException;
import com.huertos.parcelpostgres.exception.ErrorMessages;
import com.huertos.parcelpostgres.exception.InvalidParcelDataException;
import com.huertos.parcelpostgres.exception.ParcelNotFoundException;
import com.huertos.parcelpostgres.model.GeoLocation;
import com.huertos.parcelpostgres.model.Parcel;
import com.huertos.parcelpostgres.model.ParcelStatus;
import com.huertos.parcelpostgres.repository.ParcelRepository;
import com.huertos.parcelpostgres.sanitization.TextSanitizer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;

    public ParcelServiceImpl(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    @Override
    public ParcelResponse create(ParcelRequest request) {
        String name = requireCleanText(request.name(), "name", "nombre", 3, 100);
        String ownerOrLead = requireCleanText(request.ownerOrLead(), "ownerOrLead", "responsable", 3, 120);
        GeoLocationRequest location = requireLocation(request.location());

        if (parcelRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateParcelException(name);
        }

        Parcel parcel = new Parcel();
        parcel.setName(name);
        parcel.setLocation(new GeoLocation(location.latitude(), location.longitude()));
        parcel.setSoilType(request.soilType());
        parcel.setOwnerOrLead(ownerOrLead);
        parcel.setStatus(ParcelStatus.ACTIVA);
        parcel.setCreatedAt(Instant.now());

        try {
            return ParcelMapper.toResponse(parcelRepository.save(parcel));
        } catch (DataIntegrityViolationException ex) {
            if (isUniqueNameViolation(ex)) {
                throw new DuplicateParcelException(name);
            }
            throw ex;
        }
    }

    @Override
    public List<ParcelResponse> findAll() {
        return parcelRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ParcelMapper::toResponse)
                .toList();
    }

    @Override
    public ParcelResponse findById(String id) {
        UUID parcelId = parseId(id);
        return parcelRepository.findById(parcelId)
                .map(ParcelMapper::toResponse)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId.toString()));
    }

    private UUID parseId(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidParcelDataException(ErrorMessages.PARCEL_ID_INVALID);
        }
        try {
            return UUID.fromString(id.trim());
        } catch (IllegalArgumentException ex) {
            throw new InvalidParcelDataException(ErrorMessages.PARCEL_ID_INVALID);
        }
    }

    private boolean isUniqueNameViolation(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null
                ? ex.getMostSpecificCause().getMessage().toLowerCase()
                : "";
        return raw.contains("uk_parcel_name") || raw.contains("duplicate") || raw.contains("unique");
    }

    private GeoLocationRequest requireLocation(GeoLocationRequest location) {
        if (location == null || location.latitude() == null || location.longitude() == null) {
            throw new InvalidParcelDataException(
                    ErrorMessages.PARCEL_DATA_INVALID,
                    List.of(new FieldErrorDetail("location", "La ubicación GPS de la parcela es obligatoria"))
            );
        }
        if (location.latitude() < -90.0 || location.latitude() > 90.0) {
            throw new InvalidParcelDataException(
                    ErrorMessages.PARCEL_DATA_INVALID,
                    List.of(new FieldErrorDetail("location.latitude", "La latitud debe estar entre -90 y 90"))
            );
        }
        if (location.longitude() < -180.0 || location.longitude() > 180.0) {
            throw new InvalidParcelDataException(
                    ErrorMessages.PARCEL_DATA_INVALID,
                    List.of(new FieldErrorDetail("location.longitude", "La longitud debe estar entre -180 y 180"))
            );
        }
        return location;
    }

    private String requireCleanText(String value, String field, String label, int min, int max) {
        String sanitized = TextSanitizer.normalize(value);
        if (sanitized == null || sanitized.isBlank()) {
            throw new InvalidParcelDataException(
                    ErrorMessages.PARCEL_DATA_INVALID,
                    List.of(new FieldErrorDetail(field, ErrorMessages.sanitizedRequired(label)))
            );
        }
        if (TextSanitizer.hasMaliciousContent(sanitized)) {
            throw new InvalidParcelDataException(
                    ErrorMessages.PARCEL_DATA_INVALID,
                    List.of(new FieldErrorDetail(field, ErrorMessages.MALICIOUS_CONTENT))
            );
        }
        if (!TextSanitizer.isAllowedText(sanitized)) {
            throw new InvalidParcelDataException(
                    ErrorMessages.PARCEL_DATA_INVALID,
                    List.of(new FieldErrorDetail(field, ErrorMessages.TEXT_PATTERN))
            );
        }
        if (sanitized.length() < min || sanitized.length() > max) {
            throw new InvalidParcelDataException(
                    ErrorMessages.PARCEL_DATA_INVALID,
                    List.of(new FieldErrorDetail(field, ErrorMessages.sanitizedSize(label, min, max)))
            );
        }
        return sanitized;
    }
}
