package com.banklab.peerCompare.service;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.member.service.MemberService;
import com.banklab.peerCompare.dto.CategoryComparisonDTO;
import com.banklab.peerCompare.dto.PeerComparisonResponseDTO;
import com.banklab.peerCompare.mapper.ComparisonMapper;
import com.banklab.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ComparisonServiceImpl implements ComparisonService {
    private final ComparisonMapper comparisonMapper;
    private final TransactionService transactionService;
    private final MemberService memberService;

    /**
     * 1. 현재 사용자의 나이를 기준으로 또래 계산 (ex. 20~24: 5살 단위)
     * 2. 또래 그룹의 기간별 카테고리 지출 조회
     * 3. 또래 그룹의 기간별 총 지출 조회
     *
     * @param memberId  사용자 고유 ID
     * @param email     사용자 email
     * @param startDate 조회 시작일
     * @param endDate   조회 마지막일
     * @return 또래 그룹의 카테고리별 지출, 총 지출 정렬 반환
     */
    @Override
    @Transactional(readOnly = true)
    public PeerComparisonResponseDTO getPeerCategoryCompare(Long memberId, String email, Date startDate, Date endDate) {
        LocalDate now = LocalDate.now();
        if (startDate == null) {
            startDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
        }
        if (endDate == null) {
            endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
        }

        // 1. 사용자 나이 구하기
        String birth = memberService.get(memberId, email).getBirth();

        // 2. 사용자 또래 그룹 나이 범위 계산
        int age = calculateAge(birth);
        int ageFrom = (age / 5) * 5;
        int ageTo = ageFrom + 4;

        try {
            // 3. 또래 카테고리별 평균 지출 조회
            List<CategoryComparisonDTO> peerCategoryExpense = comparisonMapper.getPeerCategoryExpense(
                    memberId,
                    startDate,
                    endDate,
                    ageFrom,
                    ageTo
            );

            // 4. 또래 평균 총지출 조회
            Long peerAvgTotalExpense = comparisonMapper.getPeerTotalAvgExpense(
                    memberId,
                    startDate,
                    endDate,
                    ageFrom,
                    ageTo
            );

            // 5. 카테고리별 지출 내림차순, 지출액이 같다면 카테고리 id 오름차순 정렬
            peerCategoryExpense.sort((a, b) -> {
                int cmp = Double.compare(b.getAvgExpense(), a.getAvgExpense()); // 내림차순
                if (cmp == 0) {
                    return Long.compare(a.getCategoryId(), b.getCategoryId()); // 오름차순
                }
                return cmp;
            });

            return PeerComparisonResponseDTO.builder()
                    .categoryComparisons(peerCategoryExpense)
                    .peerAvgTotalExpense(peerAvgTotalExpense)
                    .build();

        } catch (Exception e) {
            log.error("또래 조회 중 오류 발생", e);
            throw new RuntimeException("또래 조회 중 오류 발생");
        }
    }

    private int calculateAge(String birthDateStr) {
        LocalDate birthDate = LocalDate.parse(birthDateStr);
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }
}
