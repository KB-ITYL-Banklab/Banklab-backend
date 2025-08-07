package com.banklab.peerCompare.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PeerComparisonResponseDTO {
    private Long peerAvgTotalExpense;
    private List<CategoryComparisonDTO> categoryComparisons;
}
