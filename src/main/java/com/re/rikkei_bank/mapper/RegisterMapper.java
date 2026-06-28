package com.re.rikkei_bank.mapper;

import com.re.rikkei_bank.dto.response.AccountResponse;
import com.re.rikkei_bank.dto.response.KycResponse;
import com.re.rikkei_bank.dto.response.UserResponse;
import com.re.rikkei_bank.model.Account;
import com.re.rikkei_bank.model.KycProfile;
import com.re.rikkei_bank.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterMapper {

    @Mapping(source = "role.name", target = "role")
    UserResponse toUserResponse(User user);

    AccountResponse toAccountResponse(Account account);

    KycResponse toKycResponse(KycProfile kycProfile);
}
