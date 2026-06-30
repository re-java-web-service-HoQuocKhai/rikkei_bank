package com.re.rikkei_bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.rikkei_bank.dto.request.ChangePasswordRequest;
import com.re.rikkei_bank.dto.request.LoginRequest;
import com.re.rikkei_bank.dto.response.AuthResponse;
import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to easily test controller logic
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private com.re.rikkei_bank.repository.TokenBlacklistRepository tokenBlacklistRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password");
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .username("testuser")
                .role("ROLE_CUSTOMER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "newpass", "newpass");

        Mockito.doNothing().when(authService).changePassword(any(ChangePasswordRequest.class), any(String.class));

        org.springframework.security.authentication.UsernamePasswordAuthenticationToken principal = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("testuser", null);

        mockMvc.perform(put("/api/v1/auth/change-password")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
