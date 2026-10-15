package com.huertos.parcelmongo.service;

import com.huertos.parcelmongo.dto.GeoLocationRequest;
import com.huertos.parcelmongo.dto.ParcelRequest;
import com.huertos.parcelmongo.exception.DuplicateParcelException;
import com.huertos.parcelmongo.exception.InvalidParcelDataException;
import com.huertos.parcelmongo.exception.ParcelNotFoundException;
import com.huertos.parcelmongo.model.Parcel;
import com.huertos.parcelmongo.model.ParcelStatus;
import com.huertos.parcelmongo.model.SoilType;
import com.huertos.parcelmongo.repository.ParcelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    @Test
    void createPersistsSanitizedParcelWithActiveStatus() {
        when(parcelRepository.existsByNameIgnoreCase("Huerto Norte")).thenReturn(false);
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> {
            Parcel parcel = invocation.getArgument(0);
            parcel.setId("507f1f77bcf86cd799439011");
            return parcel;
        });

        var response = parcelService.create(new ParcelRequest(
                "  Huerto   Norte ",
                new GeoLocationRequest(19.4326, -99.1332),
                SoilType.ARCILLOSO,
                "  Ana   Ruiz "
        ));

        ArgumentCaptor<Parcel> captor = ArgumentCaptor.forClass(Parcel.class);
        verify(parcelRepository).save(captor.capture());
        Parcel saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Huerto Norte");
        assertThat(saved.getOwnerOrLead()).isEqualTo("Ana Ruiz");
        assertThat(saved.getStatus()).isEqualTo(ParcelStatus.ACTIVA);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getLocation().getLatitude()).isEqualTo(19.4326);
        assertThat(response.id()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(response.status()).isEqualTo(ParcelStatus.ACTIVA);
    }

    @Test
    void createRejectsDuplicateName() {
        when(parcelRepository.existsByNameIgnoreCase("Huerto Norte")).thenReturn(true);

        assertThatThrownBy(() -> parcelService.create(validRequest()))
                .isInstanceOf(DuplicateParcelException.class)
                .hasMessage("Ya existe una parcela registrada con el nombre 'Huerto Norte'");

        verify(parcelRepository, never()).save(any());
    }

    @Test
    void createRejectsMaliciousTextEvenIfValidationWasBypassed() {
        assertThatThrownBy(() -> parcelService.create(new ParcelRequest(
                "<script>alert(1)</script>",
                new GeoLocationRequest(19.4, -99.1),
                SoilType.LIMOSO,
                "Ana Ruiz"
        )))
                .isInstanceOf(InvalidParcelDataException.class);

        verify(parcelRepository, never()).save(any());
    }

    @Test
    void findByIdReturnsCurrentStatus() {
        Parcel parcel = new Parcel();
        parcel.setId("507f1f77bcf86cd799439011");
        parcel.setName("Huerto Norte");
        parcel.setLocation(new com.huertos.parcelmongo.model.GeoLocation(19.4, -99.1));
        parcel.setSoilType(SoilType.ARENOSO);
        parcel.setOwnerOrLead("Ana Ruiz");
        parcel.setStatus(ParcelStatus.ACTIVA);
        parcel.setCreatedAt(java.time.Instant.parse("2026-10-14T18:00:00Z"));
        when(parcelRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(parcel));

        var response = parcelService.findById(" 507f1f77bcf86cd799439011 ");

        assertThat(response.name()).isEqualTo("Huerto Norte");
        assertThat(response.status()).isEqualTo(ParcelStatus.ACTIVA);
    }

    @Test
    void findByIdRejectsMalformedIdentifier() {
        assertThatThrownBy(() -> parcelService.findById("no-es-un-id"))
                .isInstanceOf(InvalidParcelDataException.class)
                .hasMessage("El identificador de la parcela no tiene un formato válido");
    }

    @Test
    void findByIdThrowsWhenParcelDoesNotExist() {
        when(parcelRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parcelService.findById("507f1f77bcf86cd799439011"))
                .isInstanceOf(ParcelNotFoundException.class)
                .hasMessage("No se encontró la parcela con id '507f1f77bcf86cd799439011'");
    }

    @Test
    void findAllMapsEveryParcel() {
        Parcel parcel = new Parcel();
        parcel.setId("507f1f77bcf86cd799439011");
        parcel.setName("Huerto Norte");
        parcel.setLocation(new com.huertos.parcelmongo.model.GeoLocation(19.4, -99.1));
        parcel.setSoilType(SoilType.ARCILLOSO);
        parcel.setOwnerOrLead("Ana Ruiz");
        parcel.setStatus(ParcelStatus.ACTIVA);
        parcel.setCreatedAt(java.time.Instant.parse("2026-10-14T18:00:00Z"));
        when(parcelRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(parcel));

        assertThat(parcelService.findAll()).hasSize(1);
    }

    private ParcelRequest validRequest() {
        return new ParcelRequest(
                "Huerto Norte",
                new GeoLocationRequest(19.4326, -99.1332),
                SoilType.ARCILLOSO,
                "Ana Ruiz"
        );
    }
}
