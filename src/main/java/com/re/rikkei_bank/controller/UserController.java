package com.re.rikkei_bank.controller;

import com.re.rikkei_bank.dto.request.UserUpdateRequest;
import com.re.rikkei_bank.dto.response.ApiResponse;
import com.re.rikkei_bank.dto.response.UserDetailResponse;
import com.re.rikkei_bank.dto.response.UserResponse;
import com.re.rikkei_bank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserResponse> response = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<UserResponse>>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long id) {
        UserDetailResponse response = userService.getUserDetail(id);
        return ResponseEntity.ok(
                ApiResponse.<UserDetailResponse>builder()
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Cập nhật thông tin người dùng thành công")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Xóa người dùng thành công")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> lockUser(@PathVariable Long id) {
        userService.lockUser(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Khóa người dùng thành công")
                        .data(null)
                        .build()
        );
    }

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Mở khóa người dùng thành công")
                        .data(null)
                        .build()
        );
    }
}
