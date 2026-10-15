package com.huertos.parcelpostgres.controller;

import com.huertos.parcelpostgres.dto.GeoLocationResponse;
import com.huertos.parcelpostgres.dto.ParcelRequest;
import com.huertos.parcelpostgres.dto.ParcelResponse;
import com.huertos.parcelpostgres.exception.ErrorMessages;
import com.huertos.parcelpostgres.exception.GlobalExceptionHandler;
import com.huertos.parcelpostgres.exception.ParcelNotFoundException;
import com.huertos.parcelpostgres.model.ParcelStatus;
import com.huertos.parcelpostgres.model.SoilType;
import com.huertos.parcelpostgres.service.ParcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ParcelController.class)
@Import(GlobalExceptionHandler.class)
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParcelService parcelService;

    @Test
    void createReturns201AndSanitizedPayload() throws Exception {
        when(parcelService.create(any(ParcelRequest.class))).thenReturn(sample());

        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Huerto   Norte  ",
                                  "location": {"latitude": 19.4326, "longitude": -99.1332},
                                  "soilType": "arcilloso",
                                  "ownerOrLead": "  Ana   Ruiz "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/parcels/3f1c2a4e-8b6d-4e1a-9c3d-1a2b3c4d5e6f"))
                .andExpect(jsonPath("$.name").value("Huerto Norte"))
                .andExpect(jsonPath("$.status").value("ACTIVA"))
                .andExpect(jsonPath("$.soilType").value("ARCILLOSO"));

        verify(parcelService).create(org.mockito.ArgumentMatchers.argThat(request ->
                "Huerto Norte".equals(request.name())
                        && "Ana Ruiz".equals(request.ownerOrLead())
                        && request.soilType() == SoilType.ARCILLOSO));
    }

    @Test
    void createRejectsBlankNameWithCustomMessage() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "location": {"latitude": 19.4326, "longitude": -99.1332},
                                  "soilType": "ARCILLOSO",
                                  "ownerOrLead": "Ana Ruiz"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Solicitud inválida"))
                .andExpect(jsonPath("$.message").value(ErrorMessages.PARCEL_DATA_INVALID))
                .andExpect(jsonPath("$.path").value("/api/v1/parcels"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'name')].message")
                        .value(hasItem("El nombre de la parcela es obligatorio")));

        verify(parcelService, never()).create(any());
    }

    @Test
    void createRejectsMaliciousContentAndInvalidCoordinates() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "<script>alert(1)</script>",
                                  "location": {"latitude": 120, "longitude": -99.1332},
                                  "soilType": "ARCILLOSO",
                                  "ownerOrLead": "Ana Ruiz"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.PARCEL_DATA_INVALID))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'location.latitude')]").exists());

        verify(parcelService, never()).create(any());
    }

    @Test
    void createRejectsUnknownSoilType() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Huerto Norte",
                                  "location": {"latitude": 19.4326, "longitude": -99.1332},
                                  "soilType": "ROCOSO",
                                  "ownerOrLead": "Ana Ruiz"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorMessages.SOIL_TYPE_INVALID));

        verify(parcelService, never()).create(any());
    }

    @Test
    void listReturnsParcels() throws Exception {
        when(parcelService.findAll()).thenReturn(List.of(sample()));

        mockMvc.perform(get("/api/v1/parcels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3f1c2a4e-8b6d-4e1a-9c3d-1a2b3c4d5e6f"))
                .andExpect(jsonPath("$[0].status").value("ACTIVA"));
    }

    @Test
    void getByIdReturnsNotFoundMessage() throws Exception {
        when(parcelService.findById("3f1c2a4e-8b6d-4e1a-9c3d-1a2b3c4d5e6f"))
                .thenThrow(new ParcelNotFoundException("3f1c2a4e-8b6d-4e1a-9c3d-1a2b3c4d5e6f"));

        mockMvc.perform(get("/api/v1/parcels/3f1c2a4e-8b6d-4e1a-9c3d-1a2b3c4d5e6f"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Parcela no encontrada"))
                .andExpect(jsonPath("$.message")
                        .value("No se encontró la parcela con id '3f1c2a4e-8b6d-4e1a-9c3d-1a2b3c4d5e6f'"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void unknownRouteReturnsCustomNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/desconocido"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorMessages.RESOURCE_NOT_FOUND));
    }

    private ParcelResponse sample() {
        return new ParcelResponse(
                "3f1c2a4e-8b6d-4e1a-9c3d-1a2b3c4d5e6f",
                "Huerto Norte",
                new GeoLocationResponse(19.4326, -99.1332),
                SoilType.ARCILLOSO,
                "Ana Ruiz",
                ParcelStatus.ACTIVA,
                Instant.parse("2026-10-14T18:00:00Z")
        );
    }
}
