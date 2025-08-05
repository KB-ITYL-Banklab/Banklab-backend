package com.banklab.product.service.creditloan;

import com.banklab.product.domain.*;
import com.banklab.product.domain.creditloan.CreditLoanOption;
import com.banklab.product.domain.creditloan.CreditLoanProduct;
import com.banklab.product.dto.creditloan.*;
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
public class CreditLoanDetailServiceImpl implements CreditLoanDetailService {

    private final CreditLoanProductMapper creditLoanProductMapper;
    private final CreditLoanOptionMapper creditLoanOptionMapper;

    @Override
    public CreditLoanWithOptionsDto getCreditLoanWithOptions(String dclsMonth, String finCoNo, String finPrdtCd) {
        try {
            log.info("신용대출 상품과 옵션 조회: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
            
            // 1. 상품 조회
            CreditLoanProduct product = creditLoanProductMapper.findByProductKey(dclsMonth, finCoNo, finPrdtCd);
            if (product == null) {
                log.warn("신용대출 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
                return null;
            }

            // 2. 옵션 조회
            List<CreditLoanOption> options = creditLoanOptionMapper.findOptionsByProduct(dclsMonth, finCoNo, finPrdtCd);
            System.out.println(options.toString());
            
            // 3. DTO 변환
            List<CreditLoanOptionDto> optionDTOs = options.stream()
                    .map(this::convertToCreditLoanOptionDTO)
                    .collect(Collectors.toList());
            
            // 4. 통합 DTO 빌드
            return buildCreditLoanWithOptionsDTO(product, optionDTOs);
            
        } catch (Exception e) {
            log.error("신용대출 상품 옵션 조회 중 오류 발생: dclsMonth={}, finCoNo={}, finPrdtCd={}", 
                     dclsMonth, finCoNo, finPrdtCd, e);
            return null;
        }
    }

    /**
     * CreditLoanOption을 CreditLoanOptionDTO로 변환
     */
    private CreditLoanOptionDto convertToCreditLoanOptionDTO(CreditLoanOption option) {
        return CreditLoanOptionDto.builder()
                .id(option.getId())
                .crdtPrdtType(option.getCrdtPrdtType())
                .crdtLendRateType(option.getCrdtLendRateType())
                .crdtLendRateTypeNm(option.getCrdtLendRateTypeNm())
                .crdtGrad1(option.getCrdtGrad1())
                .crdtGrad4(option.getCrdtGrad4())
                .crdtGrad5(option.getCrdtGrad5())
                .crdtGrad6(option.getCrdtGrad6())
                .crdtGrad10(option.getCrdtGrad10())
                .crdtGrad11(option.getCrdtGrad11())
                .crdtGrad12(option.getCrdtGrad12())
                .crdtGrad13(option.getCrdtGrad13())
                .crdtGradAvg(option.getCrdtGradAvg())
                .build();
    }

    /**
     * CreditLoanProduct와 옵션들로 CreditLoanWithOptionsDTO 빌드
     */
    private CreditLoanWithOptionsDto buildCreditLoanWithOptionsDTO(CreditLoanProduct product, List<CreditLoanOptionDto> options) {
        // 평균금리 기준 최저/최고 금리 계산
        BigDecimal minRate = options.stream()
                .map(CreditLoanOptionDto::getCrdtGradAvg)
                .filter(rate -> rate != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maxRate = options.stream()
                .map(CreditLoanOptionDto::getCrdtGradAvg)
                .filter(rate -> rate != null)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return CreditLoanWithOptionsDto.builder()
                .dclsMonth(product.getDclsMonth())
                .finCoNo(product.getFinCoNo())
                .finPrdtCd(product.getFinPrdtCd())
                .finPrdtNm(product.getFinPrdtNm())
                .korCoNm(product.getKorCoNm())
                .productType(ProductType.CREDITLOAN)
                .joinWay(product.getJoinWay())
                .crdtPrdtType(product.getCrdtPrdtType())
                .crdtPrdtTypeNm(product.getCrdtPrdtTypeNm())
                .cbName(product.getCbName())
//                .spclCnd(product.getSpclCnd())
//                .etcNote(product.getEtcNote())
                .dclsStrtDay(product.getDclsStrtDay() != null ? product.getDclsStrtDay().toString() : null)
                .dclsEndDay(product.getDclsEndDay() != null ? product.getDclsEndDay().toString() : null)
                .options(options)
                .optionCount(options.size())
                .minRate(minRate)
                .maxRate(maxRate)
                .build();
    }
}
