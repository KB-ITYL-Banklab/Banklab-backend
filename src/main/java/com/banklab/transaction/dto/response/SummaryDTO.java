package com.banklab.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SummaryDTO {
    private List<AccountSummaryDTO> accountSummaries;
}
