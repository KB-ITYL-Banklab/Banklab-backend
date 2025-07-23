package com.banklab.product.service;

import com.banklab.product.domain.*;
import com.banklab.product.dto.deposit.*;
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
public class DepositOptionsServiceImpl implements DepositOptionsService {

    private final DepositProductMapper depositProductMapper;
    private final DepositOptionMapper depositOptionMapper;

    @Override
    public DepositWithOptionsDto getDepositWithOptions(String dclsMonth, String finCoNo, String finPrdtCd) {
        try {
            log.info("예금 상품과 옵션 조회: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);

            // 1. 상품 조회
            DepositProduct product = depositProductMapper.findByProductKey(dclsMonth, finCoNo, finPrdtCd);
            if (product == null) {
                log.warn("예금 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
                return null;
            }

            // 2. 옵션 조회
            List<DepositOption> options = depositOptionMapper.findOptionsByProduct(dclsMonth, finCoNo, finPrdtCd);

            // 3. DTO 변환
            List<DepositOptionDto> optionDTOs = options.stream()
                    .map(this::convertToDepositOptionDTO)
                    .collect(Collectors.toList());

            // 4. 통합 DTO 빌드
            return buildDepositWithOptionsDTO(product, optionDTOs);

        } catch (Exception e) {
            log.error("예금 상품 옵션 조회 중 오류 발생: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                    dclsMonth, finCoNo, finPrdtCd, e);
            return null;
        }
    }

    /**
     * DepositOption을 DepositOptionDTO로 변환
     */
    private DepositOptionDto convertToDepositOptionDTO(DepositOption option) {
        return DepositOptionDto.builder()
                .id(option.getId())
                .intrRateType(option.getIntrRateType())
                .intrRateTypeNm(option.getIntrRateTypeNm())
                .saveTrm(String.valueOf(option.getSaveTrm()))
                .intrRate(option.getIntrRate())
                .intrRate2(option.getIntrRate2())
                .build();
    }

    /**
     * DepositProduct와 옵션들로 DepositWithOptionsDTO 빌드
     */
    private DepositWithOptionsDto buildDepositWithOptionsDTO(DepositProduct product, List<DepositOptionDto> options) {
        // 최저/최고 금리 계산
        BigDecimal minRate = options.stream()
                .map(DepositOptionDto::getIntrRate)
                .filter(rate -> rate != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maxRate = options.stream()
                .map(DepositOptionDto::getIntrRate2)
                .filter(rate -> rate != null)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return DepositWithOptionsDto.builder()
                .dclsMonth(product.getDclsMonth())
                .finCoNo(product.getFinCoNo())
                .finPrdtCd(product.getFinPrdtCd())
                .finPrdtNm(product.getFinPrdtNm())
                .korCoNm(product.getKorCoNm())
                .productType(ProductType.DEPOSIT)
                .joinWay(product.getJoinWay())
                .mtrtInt(product.getMtrtInt())
                .spclCnd(product.getSpclCnd())
                .joinDeny(product.getJoinDeny())
                .joinMember(product.getJoinMember())
                .etcNote(product.getEtcNote())
                .maxLimit(product.getMaxLimit())
                .dclsStrtDay(product.getDclsStrtDay())
                .dclsEndDay(product.getDclsEndDay())
                .options(options)
                .optionCount(options.size())
                .minRate(minRate)
                .maxRate(maxRate)
                .build();
    }
}
