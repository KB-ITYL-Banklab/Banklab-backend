package com.banklab.product.service.mortgage;

import com.banklab.product.domain.LoanType;
import com.banklab.product.domain.ProductType;
import com.banklab.product.dto.mortgage.MortgageLoanWithOptionsDto;
import com.banklab.product.dto.mortgage.MortgageLoanProductDto;
import com.banklab.product.dto.mortgage.MortgageLoanOptionDto;
import com.banklab.product.domain.mortgage.MortgageLoanProduct;
import com.banklab.product.domain.mortgage.MortgageLoanOption;
import com.banklab.product.mapper.MortgageLoanProductMapper;
import com.banklab.product.mapper.MortgageLoanOptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MortgageLoanDetailServiceImpl implements MortgageLoanDetailService {
    
    private final MortgageLoanProductMapper mortgageLoanProductMapper;
    private final MortgageLoanOptionMapper mortgageLoanOptionMapper;
    
    @Override
    public MortgageLoanWithOptionsDto getMortgageLoanWithOptions(String dclsMonth, String finCoNo, String finPrdtCd) {
        try {
            log.info("주택담보대출 상품과 옵션 조회: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);

            // 1. 상품 조회
            MortgageLoanProduct product = mortgageLoanProductMapper.findByProductKey(dclsMonth, finCoNo, finPrdtCd);
            if (product == null) {
                log.warn("주택담보대출 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
                return null;
            }

            // 2. 옵션 조회
            List<MortgageLoanOption> options = mortgageLoanOptionMapper.findOptionsByProduct(dclsMonth, finCoNo, finPrdtCd);

            // 3. DTO 변환
            MortgageLoanProductDto productDto = convertToProductDto(product);
            List<MortgageLoanOptionDto> optionDTOs = options.stream()
                    .map(this::convertToOptionDto)
                    .collect(Collectors.toList());

            // 4. 통합 DTO 빌드
            return buildMortgageLoanWithOptionsDTO(productDto, optionDTOs);

        } catch (Exception e) {
            log.error("주택담보대출 상품 옵션 조회 중 오류 발생: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                    dclsMonth, finCoNo, finPrdtCd, e);
            return null;
        }

    }


    private MortgageLoanProductDto convertToProductDto(MortgageLoanProduct product) {
        return MortgageLoanProductDto.builder()
                .id(product.getId())
                .dclsMonth(product.getDclsMonth())
                .finCoNo(product.getFinCoNo())
                .finPrdtCd(product.getFinPrdtCd())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .joinWay(product.getJoinWay())
                .loanInciExpn(product.getLoanInciExpn())
                .erlyRpayFee(product.getErlyRpayFee())
                .dlyRate(product.getDlyRate())
                .loanLmt(product.getLoanLmt())
                .dclsStrtDay(product.getDclsStrtDay() != null ? product.getDclsStrtDay().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) : null)
                .dclsEndDay(product.getDclsEndDay() != null ? product.getDclsEndDay().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) : null)
                .finCoSubmDay(product.getFinCoSubmDay() != null ? product.getFinCoSubmDay().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")) : null)
                .build();
    }
    
    private MortgageLoanOptionDto convertToOptionDto(MortgageLoanOption option) {
        return MortgageLoanOptionDto.builder()
                .id(option.getId())
                .dclsMonth(option.getDclsMonth())
                .finCoNo(option.getFinCoNo())
                .finPrdtCd(option.getFinPrdtCd())
                .mrtgType(option.getMrtgType())
                .mrtgTypeNm(option.getMrtgTypeNm())
                .rpayType(option.getRpayType())
                .rpayTypeNm(option.getRpayTypeNm())
                .lendRateType(option.getLendRateType())
                .lendRateTypeNm(option.getLendRateTypeNm())
                .lendRateMin(option.getLendRateMin())
                .lendRateMax(option.getLendRateMax())
                .lendRateAvg(option.getLendRateAvg())
                .build();
    }
    private MortgageLoanWithOptionsDto buildMortgageLoanWithOptionsDTO(MortgageLoanProductDto product, List<MortgageLoanOptionDto> options) {
        // 최저/최고 평균 금리 계산 (null 제외)
        BigDecimal minRate = options.stream()
                .map(MortgageLoanOptionDto::getLendRateAvg)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxRate = options.stream()
                .map(MortgageLoanOptionDto::getLendRateAvg)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return MortgageLoanWithOptionsDto.builder()
                .dclsMonth(product.getDclsMonth())
                .finCoNo(product.getFinCoNo())
                .finPrdtCd(product.getFinPrdtCd())
                .finPrdtNm(product.getFinPrdtNm())
                .korCoNm(product.getKorCoNm())
                .productType(ProductType.LOAN)
                .loanType(LoanType.MORTGAGE)
                .joinWay(product.getJoinWay())
                .loanInciExpn(product.getLoanInciExpn())
                .erlyRpayFee(product.getErlyRpayFee())
                .dlyRate(product.getDlyRate())
                .loanLmt(product.getLoanLmt())
                .dclsStrtDay(product.getDclsStrtDay())
                .dclsEndDay(product.getDclsEndDay())
                .options(options)
                .optionCount(options.size())
                .minRate(minRate)
                .maxRate(maxRate)
                .build();
    }



}
