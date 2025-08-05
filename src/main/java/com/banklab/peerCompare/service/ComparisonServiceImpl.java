package com.banklab.peerCompare.service;

import com.banklab.category.dto.CategoryExpenseDTO;
import com.banklab.member.service.MemberService;
import com.banklab.peerCompare.dto.CategoryComparisonDTO;
import com.banklab.peerCompare.mapper.ComparisonMapper;
import com.banklab.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.comparator.Comparators;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ComparisonServiceImpl implements ComparisonService{
    private final ComparisonMapper comparisonMapper;
    private final TransactionService transactionService;
    private final MemberService memberService;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryComparisonDTO> getPeerCategoryCompare(Long memberId, String email, Date startDate, Date endDate) {
        LocalDate now = LocalDate.now();
        if (startDate == null) {
            startDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
        }
        if (endDate == null) {
            endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
        }

        String birth = memberService.get(memberId, email).getBirth();

        int age =calculateAge(birth);
        int ageFrom = (age/5) *5;
        int ageTo = ageFrom+4;

        try {
            List<CategoryComparisonDTO> peerCategoryExpense = comparisonMapper.getPeerCategoryExpense(
                    memberId,
                    startDate,
                    endDate,
                    ageFrom,
                    ageTo
            );
            
            // 정렬 코드는 여기서 수행
            peerCategoryExpense.sort((a, b) -> {
                int cmp = Double.compare(b.getAvgExpense(), a.getAvgExpense()); // 내림차순
                if (cmp == 0) {
                    return Long.compare(a.getCategoryId(), b.getCategoryId()); // 오름차순
                }
                return cmp;
            });

            return  peerCategoryExpense;
        }catch (Exception e){
            log.error("또래 조회 중 오류 발생", e);
            throw new RuntimeException("또래 조회 중 오류 발생");
        }
    }

    public List<CategoryExpenseDTO> getMyCategoryCompare(Long memberId,Date startDate, Date endDate){
        LocalDate now = LocalDate.now();
        if (startDate == null) {
            startDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
        }
        if (endDate == null) {
            endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
        }
        try{
            List<CategoryExpenseDTO> users   = transactionService.getCategoryExpense(memberId, startDate, endDate);
            for(CategoryExpenseDTO dto: users){
                log.info("total: {}, days: {}",dto.getTotalExpense(), dto.getExpenseDays());
                Integer days = dto.getExpenseDays();
                long total = dto.getTotalExpense();

                double avg = (days==null || days==0) ? 0
                        :(double)total/days;
                dto.setAvgExpense(avg);
            }

            return users;
        }catch (Exception e){
            throw new  RuntimeException("사용자의 카테고리 평균을 가져오는 중 오류 발생",e);
        }
    }
    private int calculateAge(String birthDateStr){
        LocalDate birthDate = LocalDate.parse(birthDateStr);
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }
}
