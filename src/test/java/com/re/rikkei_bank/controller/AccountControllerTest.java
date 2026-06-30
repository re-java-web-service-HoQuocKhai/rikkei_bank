package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.dto.response.AccountResponse;
import com.re.rikkei_bank.dto.response.BalanceResponse;
import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private com.re.rikkei_bank.repository.TokenBlacklistRepository tokenBlacklistRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAccountById_Success() throws Exception {
        AccountResponse response = new AccountResponse();
        response.setAccountNumber("1234567890");

        when(accountService.getAccountById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("1234567890"));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void getBalance_Success() throws Exception {
        BalanceResponse response = BalanceResponse.builder()
                .accountNumber("1234567890")
                .balance(new BigDecimal("1000.00"))
                .currency("VND")
                .build();

        when(accountService.getBalance(1L, "customer")).thenReturn(response);

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken principal = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("customer", null);

        mockMvc.perform(get("/api/v1/accounts/1/balance")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
