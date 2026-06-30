package com.re.rikkei_bank.dto.request;

import com.re.rikkei_bank.model.AccountStatus;
import lombok.Data;

@Data
public class AccountUpdateRequest {
    private AccountStatus status;
}
