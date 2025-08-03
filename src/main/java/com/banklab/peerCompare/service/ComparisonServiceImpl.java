package com.banklab.peerCompare.service;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.service.MemberService;
import com.banklab.peerCompare.dto.CategoryComparisonDTO;
import com.banklab.peerCompare.mapper.ComparisonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.bytebuddy.asm.Advice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ComparisonServiceImpl implements ComparisonService{
    private final ComparisonMapper comparisonMapper;
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

        return comparisonMapper.getPeerCategoryExpense(
                memberId,
                startDate,
                endDate,
                ageFrom,
                ageTo
        );
    }

    private int calculateAge(String birthDateStr){
        LocalDate birthDate = LocalDate.parse(birthDateStr);
        return java.time.Period.between(birthDate, LocalDate.now()).getYears();
    }
}
