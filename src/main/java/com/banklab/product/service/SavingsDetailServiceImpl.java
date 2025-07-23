package com.banklab.product.service;

import com.banklab.product.domain.*;
import com.banklab.product.dto.savings.*;
import com.banklab.product.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingsDetailServiceImpl implements SavingsDetailService {

    private final SavingsProductMapper savingsProductMapper;
    private final SavingsOptionMapper savingsOptionMapper;

    @Override
    public SavingsWithOptionsDto getSavingsWithOptions(String dclsMonth, String finCoNo, String finPrdtCd) {
        try {
            System.out.println("dcls"+dclsMonth+"finCoNo"+finCoNo+"finPrdtCd"+finPrdtCd);
            log.info("적금 상품과 옵션 조회: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
            
            // 1. 상품 조회
            SavingsProduct product = savingsProductMapper.findByProductKey(dclsMonth, finCoNo, finPrdtCd);
            if (product == null) {
                log.warn("적금 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
                return null;
            }

            // 2. 옵션 조회
            List<SavingsOption> options = savingsOptionMapper.findOptionsByProduct(dclsMonth, finCoNo, finPrdtCd);
            
            // 3. DTO 변환
            List<SavingsOptionDto> optionDTOs = options.stream()
                    .map(this::convertToSavingsOptionDto)
                    .collect(Collectors.toList());
            
            // 4. 통합 DTO 빌드
            return buildSavingsWithOptionsDto(product, optionDTOs);
            
        } catch (Exception e) {
            log.error("적금 상품 옵션 조회 중 오류 발생: dclsMonth={}, finCoNo={}, finPrdtCd={}", 
                     dclsMonth, finCoNo, finPrdtCd, e);
            return null;
        }
    }


    /**
     * SavingsOption을 SavingsOptionDTO로 변환
     */
    private SavingsOptionDto convertToSavingsOptionDto(SavingsOption option) {
        return SavingsOptionDto.builder()
                .id(option.getId())
                .intrRateType(option.getIntrRateType())
                .intrRateTypeNm(option.getIntrRateTypeNm())
                .saveTrm(String.valueOf(option.getSaveTrm()))
                .intrRate(option.getIntrRate())
                .intrRate2(option.getIntrRate2())
                .rsrvType(option.getRsrvType())
                .rsrvTypeNm(option.getRsrvTypeNm())
                .build();
    }

    /**
     * SavingsProduct와 옵션들로 SavingsWithOptionsDTO 빌드
     */
    private SavingsWithOptionsDto buildSavingsWithOptionsDto(SavingsProduct product, List<SavingsOptionDto> options) {
        // 최저/최고 금리 계산
        BigDecimal minRate = options.stream()
                .map(SavingsOptionDto::getIntrRate)
                .filter(rate -> rate != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maxRate = options.stream()
                .map(SavingsOptionDto::getIntrRate2)
                .filter(rate -> rate != null)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return SavingsWithOptionsDto.builder()
                .dclsMonth(product.getDclsMonth())
                .finCoNo(product.getFinCoNo())
                .finPrdtCd(product.getFinPrdtCd())
                .finPrdtNm(product.getFinPrdtNm())
                .korCoNm(product.getKorCoNm())
                .productType(ProductType.SAVINGS)
                .joinWay(product.getJoinWay())
                .mtrtInt(product.getMtrtInt())
                .spclCnd(product.getSpclCnd())
                .joinDeny(product.getJoinDeny())
                .joinMember(product.getJoinMember())
                .etcNote(product.getEtcNote())
                .maxLimit(product.getMaxLimit())
                .dclsStrtDay(product.getDclsStrtDay() != null ? product.getDclsStrtDay().toString() : null)
                .dclsEndDay(product.getDclsEndDay() != null ? product.getDclsEndDay().toString() : null)
                .options(options)
                .optionCount(options.size())
                .minRate(minRate)
                .maxRate(maxRate)
                .build();
    }
}
