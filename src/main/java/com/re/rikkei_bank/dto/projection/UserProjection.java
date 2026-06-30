package com.re.rikkei_bank.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProjection {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String roleName;
    private Boolean isActive;
    private Boolean isKyc;
    private LocalDateTime createdAt;
}
