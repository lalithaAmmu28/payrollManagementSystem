package com.pms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.dto.employee.EmployeeCreateRequest;
import com.pms.backend.dto.employee.EmployeeResponse;
import com.pms.backend.service.EmployeeService;
import com.pms.backend.service.SalaryStructureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EmployeeService employeeService;
    @MockBean private SalaryStructureService salaryStructureService;

    @Test
    void testGetEmployees_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/employees")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEmployees_EmployeeRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/employees")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEmployees_AdminRole_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/employees")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_AdminRole_ShouldReturn201() throws Exception {
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setUsername("jdoe");
        req.setEmail("jdoe@example.com");
        req.setPassword("secret12");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDateOfBirth(LocalDate.of(1990,1,1));
        req.setJobId("job-1");
        req.setDepartmentId("dept-1");

        when(employeeService.createEmployee(any(EmployeeCreateRequest.class))).thenReturn(new EmployeeResponse());

        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}


