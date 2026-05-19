package com.sw.sw_ai_doc.domain.health.controller;

import tools.jackson.databind.ObjectMapper;
import com.sw.sw_ai_doc.domain.health.dto.HealthResponseDto;
import com.sw.sw_ai_doc.domain.health.service.HealthService;
import com.sw.sw_ai_doc.global.exception.HealthNotFoundException;
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
import java.time.LocalDateTime;
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
class HealthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HealthService healthService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private HealthResponseDto buildResponse(Long id) {
        return HealthResponseDto.builder()
                .healthId(id)
                .userId(1L)
                .symptom("두통")
                .history("고혈압")
                .note("메모")
                .recordDate(LocalDate.of(2024, 5, 1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Map<String, Object> validHealthBody() {
        return Map.of(
                "symptom", "두통",
                "history", "고혈압",
                "note", "메모",
                "recordDate", "2024-05-01"
        );
    }

    @Test
    void createHealth_success() throws Exception {
        given(healthService.createHealth(eq(1L), any())).willReturn(buildResponse(1L));

        mockMvc.perform(post("/api/health/create")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validHealthBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.symptom").value("두통"))
                .andExpect(jsonPath("$.data.healthId").value(1));
    }

    @Test
    void createHealth_blankSymptom_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "symptom", "",
                "recordDate", "2024-05-01"
        );

        mockMvc.perform(post("/api/health/create")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createHealth_missingRecordDate_returns400() throws Exception {
        Map<String, Object> body = Map.of("symptom", "두통");

        mockMvc.perform(post("/api/health/create")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHealthList_success() throws Exception {
        given(healthService.getHealthList(1L)).willReturn(List.of(buildResponse(1L), buildResponse(2L)));

        mockMvc.perform(get("/api/health/list")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getHealthDetail_success() throws Exception {
        given(healthService.getHealthDetail(1L, 1L)).willReturn(buildResponse(1L));

        mockMvc.perform(get("/api/health/1")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.healthId").value(1));
    }

    @Test
    void getHealthDetail_notFound_returns404() throws Exception {
        given(healthService.getHealthDetail(999L, 1L)).willThrow(new HealthNotFoundException());

        mockMvc.perform(get("/api/health/999")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateHealth_success() throws Exception {
        given(healthService.updateHealth(eq(1L), eq(1L), any())).willReturn(buildResponse(1L));

        mockMvc.perform(put("/api/health/update/1")
                        .with(SecurityTestHelper.mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validHealthBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteHealth_success() throws Exception {
        mockMvc.perform(delete("/api/health/delete/1")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteHealth_notFound_returns404() throws Exception {
        doThrow(new HealthNotFoundException()).when(healthService).deleteHealth(999L, 1L);

        mockMvc.perform(delete("/api/health/delete/999")
                        .with(SecurityTestHelper.mockUser()))
                .andExpect(status().isNotFound());
    }
}
