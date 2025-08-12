package com.banklab.transaction.mapper;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.TransactionDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TransactionMapper {
    int saveTransaction(TransactionHistoryVO transaction);

    int saveTransactionList(List<TransactionHistoryVO> list);

    LocalDate getLastTransactionDate(@Param("memberId") Long memberId, @Param("resAccount") String resAccount);

    void updateCategories(@Param("list") List<TransactionHistoryVO> transactions);

    /**
     * 특정 상호명 거래 내역 카테고리 일괄 update
     */
    void updateCategoryByDesc(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId, @Param("description") String description);

    MonthlySummaryDTO getMonthlySummary(
            @Param("memberId") Long memberId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    List<DailyExpenseDTO> getDailyExpense(
            @Param("memberId") Long memberId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


    List<CategoryExpenseDTO> getExpensesByCategory(
            @Param("memberId") Long memberId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    /**
     * 계좌 ID로 거래내역 상세 조회 (DTO 반환)
     */
    List<TransactionDetailDTO> getTransactionDetailsByAccountId(
            @Param("memberId") Long memberId,
            @Param("resAccount") String resAccount,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );


    /**
     * 카테고리 ID로 거래내역 상세 조회 (DTO 반환)
     */
    List<TransactionDetailDTO> getTransactionDetailsByCategoryId(
            @Param("memberId") Long memberId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 특정 상호명 거래 일자 구하기 (카테고리 업데이트 시 사용)
     */
    List<Date> getTransactionDates(@Param("memberId") Long memberId, @Param("description") String description);
}
