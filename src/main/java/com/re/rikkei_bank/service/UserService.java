package com.re.rikkei_bank.service;

import com.re.rikkei_bank.dto.request.UserUpdateRequest;
import com.re.rikkei_bank.dto.projection.UserProjection;
import com.re.rikkei_bank.dto.request.UserUpdateRequest;
import com.re.rikkei_bank.dto.response.PageResponse;
import com.re.rikkei_bank.dto.response.UserDetailResponse;
import com.re.rikkei_bank.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PageResponse<UserProjection> searchUsers(String keyword, String cccd, Boolean status, String roleName, Pageable pageable);
    UserDetailResponse getUserDetail(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
    void lockUser(Long id);
    void unlockUser(Long id);
}
