package com.re.rikkei_bank.dto.request;

import com.re.rikkei_bank.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class ResubmitKycRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "ID number is required")
    @Pattern(regexp = "^\\d{12}$", message = "ID number (CCCD) must be exactly 12 digits")
    private String idNumber;

    @NotNull(message = "Date of birth is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "CCCD front image is required")
    private MultipartFile cccdFront;

    @NotNull(message = "CCCD back image is required")
    private MultipartFile cccdBack;

    @NotNull(message = "Selfie image is required")
    private MultipartFile selfie;
}
