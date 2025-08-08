package com.banklab.product.service.annuity;

import com.banklab.product.domain.annuity.AnnuityOption;
import com.banklab.product.domain.annuity.AnnuityProduct;
import com.banklab.product.dto.annuity.AnnuityOptionDto;
import com.banklab.product.dto.annuity.AnnuityWithOptionsDto;
import com.banklab.product.mapper.AnnuityOptionMapper;
import com.banklab.product.mapper.AnnuityProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnuityDetailServiceImpl implements AnnuityDetailService {

    private final AnnuityProductMapper annuityProductMapper;
    private final AnnuityOptionMapper annuityOptionMapper;

    @Override
    public AnnuityWithOptionsDto getAnnuityProductsWithOptions(String dclsMonth, String finCoNo, String finPrdtCd) {
        try {
            // 1. 상품 조회
            AnnuityProduct product = annuityProductMapper.findByProductKey(dclsMonth, finCoNo, finPrdtCd);
            if (product == null) {
                log.warn("연금 저축 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
                return null;
            }

            // 2. 옵션 조회
            List<AnnuityOption> options = annuityOptionMapper.findOptionsByProduct(dclsMonth, finCoNo, finPrdtCd);

            // 3. DTO 변환
            List<AnnuityOptionDto> optionDTOs = options.stream()
                    .map(this::convertToAnnuityOptionDto)
                    .collect(Collectors.toList());

            // 4. 통합 DTO 생성
            return buildAnnuityWithOptionsDto(product, optionDTOs);

        } catch (Exception e) {
            log.error("연금 저축 옵션 조회 중 오류 발생: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd, e);
            return null;
        }
    }

    /**
     * AnnuityOption을 AnnuityOptionDto로 변환
     * @param option 연금 옵션 엔티티
     * @return 변환된 DTO
     */
    private AnnuityOptionDto convertToAnnuityOptionDto(AnnuityOption option) {
        return AnnuityOptionDto.builder()
                .id(option.getId())
                .pnsnRecpTrm(option.getPnsnRecpTrm())
                .pnsnRecpTrmNm(option.getPnsnRecpTrmNm())
                .pnsnEntrAge(option.getPnsnEntrAge())
                .pnsnEntrAgeNm(option.getPnsnEntrAgeNm())
                .monPaymAtm(option.getMonPaymAtm())
                .monPaymAtmNm(option.getMonPaymAtmNm())
                .paymPrd(option.getPaymPrd())
                .paymPrdNm(option.getPaymPrdNm())
                .pnsnStrtAge(option.getPnsnStrtAge())
                .pnsnStrtAgeNm(option.getPnsnStrtAgeNm())
                .pnsnRecpAmt(option.getPnsnRecpAmt())
                .build();



    }

    /**
     * AnnuityProduct와 옵션 리스트를 통합 DTO로 변환
     * @param product 연금 상품 엔티티
     * @param options 옵션 DTO 리스트
     * @return 통합된 DTO
     */
    private AnnuityWithOptionsDto buildAnnuityWithOptionsDto(AnnuityProduct product, List<AnnuityOptionDto> options) {
        BigDecimal minRecpAmt = options.stream()
                .map(AnnuityOptionDto::getPnsnRecpAmt)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxRecpAmt = options.stream()
                .map(AnnuityOptionDto::getPnsnRecpAmt)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return AnnuityWithOptionsDto.builder()
                .dclsMonth(product.getDclsMonth())
                .finCoNo(product.getFinCoNo())
                .finPrdtCd(product.getFinPrdtCd())
                .finPrdtNm(product.getFinPrdtNm())
                .korCoNm(product.getKorCoNm())
                .joinWay(product.getJoinWay())
                .pnsnKind(product.getPnsnKind())
                .pnsnKindNm(product.getPnsnKindNm())
                .prdtType(product.getPrdtType())
                .prdtTypeNm(product.getPrdtTypeNm())
                .saleStrtDay(product.getSaleStrtDay())
                .mntnCnt(product.getMntnCnt())
                .saleCo(product.getSaleCo())
                .dclsStrtDay(product.getDclsStrtDay()!=null ? product.getDclsStrtDay().toString() : null)
                .dclsEndDay(product.getDclsEndDay()!=null ? product.getDclsEndDay().toString() : null)
                .avgPrftRate(product.getAvgPrftRate())
                .dclsRate(product.getDclsRate())
                .guarRate(product.getGuarRate())
                .btrmPrftRate1(product.getBtrmPrftRate1())
                .btrmPrftRate2(product.getBtrmPrftRate2())
                .btrmPrftRate3(product.getBtrmPrftRate3())
                .optionCount(options.size())
                .minRecpAmt(minRecpAmt)
                .maxRecpAmt(maxRecpAmt)
                .options(options)
                .build();
    }
}
