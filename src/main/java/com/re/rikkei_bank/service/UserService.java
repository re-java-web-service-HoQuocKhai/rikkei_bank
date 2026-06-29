package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.UserUpdateRequest;
import com.re.rikkei_bank.dto.response.UserDetailResponse;
import com.re.rikkei_bank.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);
    UserDetailResponse getUserDetail(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    void lockUser(Long id);
    void unlockUser(Long id);
}
