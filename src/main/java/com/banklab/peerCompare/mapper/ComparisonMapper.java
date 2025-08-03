package com.banklab.peerCompare.mapper;

import com.banklab.peerCompare.dto.CategoryComparisonDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ComparisonMapper {
    /**
     * 
     * @param memberId 사용자 id
     * @param startDate 조회 시작일
     * @param endDate   조회 마지막일
     * @param ageFrom   나이 시작
     * @param ageTo     나이 끝
     * @return
     */
    List<CategoryComparisonDTO> getPeerCategoryExpense(
            @Param("membberId") Long memberId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("ageFrom") Integer ageFrom,
            @Param("ageTo") Integer ageTo
    );
}
