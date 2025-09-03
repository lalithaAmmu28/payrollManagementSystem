package com.pms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.dto.department.DepartmentCreateRequest;
import com.pms.backend.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepartmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DepartmentService departmentService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateDepartment_InvalidPayload_ShouldReturn400() throws Exception {
        // Given blank name which violates @NotBlank in DepartmentCreateRequest
        DepartmentCreateRequest req = new DepartmentCreateRequest();
        req.setDepartmentName("");

        // When / Then
        mockMvc.perform(post("/api/v1/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}


