package com.re.rikkei_bank.mapper;

import com.re.rikkei_bank.dto.response.AccountResponse;
import com.re.rikkei_bank.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountResponse toAccountResponse(Account account);
}
