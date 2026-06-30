package com.re.rikkei_bank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.re.rikkei_bank.dto.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Access Denied: Bạn không có quyền truy cập tài nguyên này.")
                .errorCode("ACCESS_DENIED")
                .timestamp(LocalDateTime.now())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // Support LocalDateTime
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
