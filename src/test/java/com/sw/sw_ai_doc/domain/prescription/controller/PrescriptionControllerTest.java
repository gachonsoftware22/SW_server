package com.sw.sw_ai_doc.domain.prescription.controller;

import tools.jackson.databind.ObjectMapper;
import com.sw.sw_ai_doc.domain.prescription.dto.PrescriptionResponseDto;
import com.sw.sw_ai_doc.domain.prescription.service.PrescriptionService;
import com.sw.sw_ai_doc.global.exception.PrescriptionNotFoundException;
import com.sw.sw_ai_doc.support.SecurityTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class PrescriptionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PrescriptionService prescriptionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private PrescriptionResponseDto buildResponse(Long id) {
        return PrescriptionResponseDto.builder()
                .prescriptionId(id)
                .prescriptionDate(LocalDate.of(2024, 3, 1))
                .hospitalName("서울병원")
                .medicineList(List.of(
                        PrescriptionResponseDto.MedicineResponseDto.builder()
                                .medicineName("아스피린")
                                .dosage("1정")
                                .duration("7일")
                                .build()
                ))
                .build();
    }

    private Map<String, Object> validPrescriptionBody() {
        return Map.of(
                "prescriptionDate", "2024-03-01",
                "hospitalName", "서울병원",
                "medicineList", List.of(
                        Map.of("medicineName", "아스피린", "dosage", "1정", "duration", "7일")
                )
        );
    }

    @Test
    void createPrescription_success() throws Exception {
        given(prescriptionService.createPrescription(eq(1L), any())).willReturn(buildResponse(1L));

        mockMvc.perform(post("/api/prescription/create")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPrescriptionBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.hospitalName").value("서울병원"))
                .andExpect(jsonPath("$.data.prescriptionId").value(1));
    }

    @Test
    void createPrescription_missingMedicineList_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "prescriptionDate", "2024-03-01",
                "hospitalName", "서울병원"
        );

        mockMvc.perform(post("/api/prescription/create")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createPrescription_missingHospitalName_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "prescriptionDate", "2024-03-01",
                "medicineList", List.of(
                        Map.of("medicineName", "아스피린", "dosage", "1정", "duration", "7일")
                )
        );

        mockMvc.perform(post("/api/prescription/create")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPrescriptionList_success() throws Exception {
        given(prescriptionService.getPrescriptionList(1L))
                .willReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get("/api/prescription/list")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getPrescriptionDetail_success() throws Exception {
        given(prescriptionService.getPrescriptionDetail(1L, 1L)).willReturn(buildResponse(1L));

        mockMvc.perform(get("/api/prescription/1")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prescriptionId").value(1));
    }

    @Test
    void getPrescriptionDetail_notFound_returns404() throws Exception {
        given(prescriptionService.getPrescriptionDetail(999L, 1L))
                .willThrow(new PrescriptionNotFoundException());

        mockMvc.perform(get("/api/prescription/999")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updatePrescription_success() throws Exception {
        mockMvc.perform(put("/api/prescription/update/1")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPrescriptionBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deletePrescription_success() throws Exception {
        mockMvc.perform(delete("/api/prescription/delete/1")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deletePrescription_notFound_returns404() throws Exception {
        doThrow(new PrescriptionNotFoundException())
                .when(prescriptionService).deletePrescription(999L, 1L);

        mockMvc.perform(delete("/api/prescription/delete/999")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isNotFound());
    }
}
