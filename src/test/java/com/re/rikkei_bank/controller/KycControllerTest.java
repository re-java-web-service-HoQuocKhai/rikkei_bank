package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.KycService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KycController.class)
@AutoConfigureMockMvc(addFilters = false)
class KycControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KycService kycService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private com.re.rikkei_bank.repository.TokenBlacklistRepository tokenBlacklistRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void approveKyc_Success() throws Exception {
        doNothing().when(kycService).approveKyc(1L);

        mockMvc.perform(put("/api/v1/kyc/1/approve")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void rejectKyc_Success() throws Exception {
        com.re.rikkei_bank.dto.request.RejectKycRequest request = new com.re.rikkei_bank.dto.request.RejectKycRequest();
        request.setRejectReason("Invalid ID");
        doNothing().when(kycService).rejectKyc(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(put("/api/v1/kyc/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rejectReason\":\"Invalid ID\"}")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
