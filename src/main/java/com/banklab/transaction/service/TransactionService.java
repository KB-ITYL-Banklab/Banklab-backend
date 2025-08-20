package com.banklab.transaction.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.banklab.transaction.dto.request.TransactionRequestDto;
import com.banklab.transaction.dto.response.DailyExpenseDTO;
import com.banklab.transaction.dto.response.MonthlySummaryDTO;
import com.banklab.transaction.dto.response.SummaryDTO;
import com.banklab.transaction.dto.response.TransactionDetailDTO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TransactionService {
    /**
     * 조회된 거래 내역 리스트를 DB에 저장합니다.
     * @param memberId 사용자 ID
     * @param account 계좌 정보
     * @param transactionVOList 저장할 거래 내역 리스트
     */
    void saveTransactionList(Long memberId, AccountVO account, List<TransactionHistoryVO> transactionVOList);

    /**
     * 특정 계좌의 마지막 거래 일자를 조회합니다.
     * @param memberId 사용자 ID
     * @param account 계좌 번호
     * @return 마지막 거래 일자 (LocalDate)
     */
    LocalDate getLastTransactionDay(Long memberId, String account);

    /**
     * 거래 내역 리스트의 카테고리 정보를 업데이트합니다.
     * @param transactions 카테고리 정보가 포함된 거래 내역 리스트
     */
    void updateCategories(List<TransactionHistoryVO> transactions);

    /**
     * 특정 상호명(description)을 가진 모든 거래 내역의 카테고리를 일괄 변경합니다.
     * @param categoryId 변경할 카테고리 ID
     * @param desc 대상 상호명
     * @param memberId 사용자 ID
     */
    void updateCategoryByDesc(Long categoryId, String desc, Long memberId);

    /**
     * 지정된 기간 동안의 소비/수입 내역을 요약하여 제공합니다.
     * (월별, 일별, 주별, 카테고리별 요약 포함)
     * @param memberId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 요약 정보 DTO
     */
    SummaryDTO getSummary(Long memberId, Date startDate, Date endDate);

    /**
     * 지정된 기간 동안의 총 수입과 총 지출을 조회합니다.
     * @param memberId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 월별 요약 DTO
     */
    MonthlySummaryDTO getMonthlySummary(Long memberId, Date startDate, Date endDate);

    /**
     * 지정된 기간 동안의 일별 지출 내역을 조회합니다.
     * @param memberId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 일별 지출 DTO 리스트
     */
    List<DailyExpenseDTO> getDailyExpense(Long memberId, Date startDate, Date endDate);

    /**
     * 지정된 기간 동안의 카테고리별 지출 내역을 조회합니다.
     * @param memberId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 카테고리별 지출 DTO 리스트
     */
    List<CategoryExpenseDTO> getCategoryExpense(Long memberId, Date startDate, Date endDate);


    /**
     * 계좌 ID로 거래내역 상세 조회 (계좌 소유권 검증 포함)
     * @param memberId  회원 ID
     * @param accountId 계좌 ID (account 테이블의 PK)
     * @param startDate 조회 시작일
     * @param endDate   조회 종료일
     * @return 거래내역 상세 DTO 리스트
     */
    List<TransactionDetailDTO> getTransactionDetailsByAccountId(Long memberId, Long accountId, Date startDate, Date endDate);

    /**
     * 카테고리 ID로 특정 기간의 거래 내역 상세 정보를 조회합니다.
     * @param memberId 사용자 ID
     * @param categoryId 카테고리 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 거래 내역 상세 DTO 리스트
     */
    List<TransactionDetailDTO> getTransactionDetailsByCategoryId(Long memberId, Long categoryId, Date startDate, Date endDate);

    /**
     * 특정 상호명을 가진 거래들의 발생 일자 리스트를 조회합니다. (카테고리 변경 후 집계 업데이트 시 사용)
     * @param memberId 사용자 ID
     * @param description 상호명
     * @return 날짜(Date) 리스트
     */
    List<Date> getTransactionDates(Long memberId, String description);
}
