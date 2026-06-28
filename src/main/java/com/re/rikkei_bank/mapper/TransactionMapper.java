package com.re.rikkei_bank.mapper;

import com.re.rikkei_bank.dto.response.TransactionResponse;
import com.re.rikkei_bank.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "fromAccount.accountNumber", target = "fromAccountNumber")
    @Mapping(source = "toAccount.accountNumber", target = "toAccountNumber")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
