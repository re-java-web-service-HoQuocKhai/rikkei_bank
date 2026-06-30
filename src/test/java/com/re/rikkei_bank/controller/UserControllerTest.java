package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.dto.response.UserDetailResponse;
import com.re.rikkei_bank.dto.response.UserResponse;
import com.re.rikkei_bank.security.JwtProvider;
import com.re.rikkei_bank.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private com.re.rikkei_bank.repository.TokenBlacklistRepository tokenBlacklistRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserDetail_Success() throws Exception {
        UserDetailResponse mockResponse = new UserDetailResponse();
        mockResponse.setUsername("testuser");

        when(userService.getUserDetail(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }
}
