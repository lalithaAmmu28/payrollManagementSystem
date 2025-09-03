package com.pms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.dto.jobrole.JobRoleCreateRequest;
import com.pms.backend.dto.jobrole.JobRoleResponse;
import com.pms.backend.service.JobRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobRoleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private JobRoleService jobRoleService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createJobRole_Admin_ShouldReturn201() throws Exception {
        when(jobRoleService.createJobRole(any(JobRoleCreateRequest.class))).thenReturn(new JobRoleResponse());

        JobRoleCreateRequest req = new JobRoleCreateRequest();
        req.setJobTitle("Engineer");
        req.setBaseSalary(java.math.BigDecimal.valueOf(600000));

        mockMvc.perform(post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}


