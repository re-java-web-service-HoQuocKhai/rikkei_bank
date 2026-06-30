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
public class ChangePinRequest {
    @NotBlank(message = "Mã PIN cũ không được để trống")
    private String oldPin;

    @NotBlank(message = "Mã PIN mới không được để trống")
    @Size(min = 6, max = 6, message = "Mã PIN phải có đúng 6 chữ số")
    private String newPin;

    @NotBlank(message = "Xác nhận mã PIN không được để trống")
    private String confirmPin;
}
