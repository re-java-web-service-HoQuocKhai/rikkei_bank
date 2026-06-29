package com.re.rikkei_bank.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    private Long roleId;
}
