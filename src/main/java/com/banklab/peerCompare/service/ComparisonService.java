package com.banklab.peerCompare.service;

import com.banklab.peerCompare.dto.PeerComparisonResponseDTO;

import java.util.Date;

public interface ComparisonService {
    PeerComparisonResponseDTO getPeerCategoryCompare(
            Long memberId,
            String email,
            Date startDate,
            Date endDate);

    PeerComparisonResponseDTO compareWithPeer(Long memberId, String startDate, String endDate);
//    List<CategoryExpenseDTO> getMyCategoryCompare(Long memberId,Date startDate, Date endDate);
}
