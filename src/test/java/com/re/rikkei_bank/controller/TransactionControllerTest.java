package com.re.rikkei_bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.rikkei_bank.dto.request.TransferRequest;
import com.re.rikkei_bank.dto.response.TransferResponse;
import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private com.re.rikkei_bank.repository.TokenBlacklistRepository tokenBlacklistRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "senderUser", roles = "CUSTOMER")
    void transfer_Success() throws Exception {
        TransferRequest request = new TransferRequest("111111", "222222", new BigDecimal("100.00"), "Transfer", "123456");
        TransferResponse response = new TransferResponse("TX123", "SUCCESS", "Transfer");

        when(transactionService.transfer(any(TransferRequest.class), anyString())).thenReturn(response);

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken principal = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("senderUser", null);

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.success").value(true));
    }
}
