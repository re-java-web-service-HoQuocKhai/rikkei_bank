package com.re.rikkei_bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, message = "Mật khẩu mới phải từ 8 ký tự trở lên")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @Size(min = 8, message = "Xác nhận mật khẩu phải từ 8 ký tự trở lên")
    private String confirmPassword;
}
