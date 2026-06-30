package com.re.rikkei_bank.service.impl;

import com.re.rikkei_bank.dto.projection.UserProjection;
import com.re.rikkei_bank.dto.request.UserUpdateRequest;
import com.re.rikkei_bank.dto.response.AccountResponse;
import com.re.rikkei_bank.dto.response.KycResponse;
import com.re.rikkei_bank.dto.response.PageResponse;
import com.re.rikkei_bank.dto.response.UserDetailResponse;
import com.re.rikkei_bank.dto.response.UserResponse;
import com.re.rikkei_bank.exception.CustomException;
import com.re.rikkei_bank.exception.DuplicateResourceException;
import com.re.rikkei_bank.exception.UserNotFoundException;
import com.re.rikkei_bank.mapper.RegisterMapper;
import com.re.rikkei_bank.model.Account;
import com.re.rikkei_bank.model.KycProfile;
import com.re.rikkei_bank.model.Role;
import com.re.rikkei_bank.model.User;
import com.re.rikkei_bank.repository.*;
import com.re.rikkei_bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final KycProfileRepository kycProfileRepository;
    private final TransactionRepository transactionRepository;
    private final RegisterMapper registerMapper;

    @Override
    public PageResponse<UserProjection> searchUsers(String keyword, String cccd, Boolean status, String roleName, Pageable pageable) {
        Page<UserProjection> users = userRepository.searchUsers(keyword, cccd, status, roleName, pageable);
        return PageResponse.of(users);
    }

    @Override
    public UserDetailResponse getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với id: " + id));

        UserResponse baseResponse = registerMapper.toUserResponse(user);
        
        Optional<Account> accountOpt = accountRepository.findByUserId(id);
        AccountResponse accountResponse = accountOpt.map(registerMapper::toAccountResponse).orElse(null);

        Optional<KycProfile> kycOpt = kycProfileRepository.findByUserId(id);
        KycResponse kycResponse = kycOpt.map(registerMapper::toKycResponse).orElse(null);

        return UserDetailResponse.builder()
                .id(baseResponse.getId())
                .username(baseResponse.getUsername())
                .email(baseResponse.getEmail())
                .phoneNumber(baseResponse.getPhoneNumber())
                .role(baseResponse.getRole())
                .isActive(baseResponse.getIsActive())
                .isKyc(baseResponse.getIsKyc())
                .createdAt(baseResponse.getCreatedAt())
                .account(accountResponse)
                .kycProfile(kycResponse)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với id: " + id));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new DuplicateResourceException("Email đã được sử dụng bởi người dùng khác");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            if (userRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id)) {
                throw new DuplicateResourceException("Số điện thoại đã được sử dụng bởi người dùng khác");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new CustomException("Role không tồn tại", HttpStatus.BAD_REQUEST));
            user.setRole(role);
        }

        User updatedUser = userRepository.save(user);
        return registerMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với id: " + id));

        if (transactionRepository.hasTransactionsByUserId(id)) {
            throw new CustomException("Người dùng đã có lịch sử giao dịch. Không thể xóa cứng, vui lòng sử dụng chức năng Khóa tài khoản (Lock) thay thế.", HttpStatus.BAD_REQUEST);
        }

        kycProfileRepository.findByUserId(id).ifPresent(kycProfileRepository::delete);
        accountRepository.findByUserId(id).ifPresent(accountRepository::delete);
        userRepository.delete(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với id: " + id));
        user.setIsActive(true);
        userRepository.save(user);
    }
}
