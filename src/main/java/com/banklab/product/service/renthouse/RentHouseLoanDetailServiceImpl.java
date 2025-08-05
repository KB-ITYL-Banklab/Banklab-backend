package com.banklab.product.service.renthouse;

import com.banklab.product.domain.ProductType;
import com.banklab.product.domain.renthouse.RentHouseLoanOption;
import com.banklab.product.domain.renthouse.RentHouseLoanProduct;
import com.banklab.product.dto.renthouse.RentHouseLoanOptionDto;
import com.banklab.product.dto.renthouse.RentHouseLoanWithOptionsDto;
import com.banklab.product.mapper.RentHouseLoanOptionMapper;
import com.banklab.product.mapper.RentHouseLoanProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RentHouseLoanDetailServiceImpl implements RentHouseLoanDetailService {

    private final RentHouseLoanProductMapper rentHouseLoanProductMapper;
    private final RentHouseLoanOptionMapper rentHouseLoanOptionMapper;

    @Override
    public RentHouseLoanWithOptionsDto getRentHouseLoanWithOptions(String dclsMonth, String finCoNo, String finPrdtCd) {
        try {
            System.out.println("dcls"+dclsMonth+"finCoNo"+finCoNo+"finPrdtCd"+finPrdtCd);
            log.info("전세자금대출 상품과 옵션 조회: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);

            // 1. 상품 조회
            RentHouseLoanProduct product = rentHouseLoanProductMapper.findByProductKey(dclsMonth, finCoNo, finPrdtCd);
            if (product == null) {
                log.warn("전세자금대출 상품을 찾을 수 없음: dclsMonth={}, finCoNo={}, finPrdtCd={}", dclsMonth, finCoNo, finPrdtCd);
                return null;
            }

            // 2. 옵션 조회
            List<RentHouseLoanOption> options = rentHouseLoanOptionMapper.findOptionsByProduct(dclsMonth, finCoNo, finPrdtCd);

            // 3. DTO 변환
            List<RentHouseLoanOptionDto> optionsDTOs = options.stream()
                    .map(this::convertToOptionDto)
                    .collect(Collectors.toList());

            // 4. 통합 DTO 빌드
            return buildRentHouseLoanWithOptionsDto(product, optionsDTOs);


        } catch (Exception e) {
            log.error("전세자금대출 상품 옵션 조회 중 오류 발생: dclsMonth={}, finCoNo={}, finPrdtCd={}",
                    dclsMonth, finCoNo, finPrdtCd, e);
            return null;
        }
    }

    private RentHouseLoanWithOptionsDto buildRentHouseLoanWithOptionsDto(RentHouseLoanProduct product, List<RentHouseLoanOptionDto> options) {
        if (product == null) return null;
        BigDecimal minRate = options.stream()
                .map(RentHouseLoanOptionDto::getLendRateMin)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxRate = options.stream()
                .map(RentHouseLoanOptionDto::getLendRateMax)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);


        return RentHouseLoanWithOptionsDto.builder()
                .dclsMonth(product.getDclsMonth())
                .finCoNo(product.getFinCoNo())
                .finPrdtCd(product.getFinPrdtCd())
                .korCoNm(product.getKorCoNm())
                .finPrdtNm(product.getFinPrdtNm())
                .productType(ProductType.RENTHOUSE)
                .joinWay(product.getJoinWay())
                .loanInciExpn(product.getLoanInciExpn())
                .erlyRpayFee(product.getErlyRpayFee())
                .dlyRate(product.getDlyRate())
                .loanLmt(product.getLoanLmt())
                .dclsStrtDay(String.valueOf(product.getDclsStrtDay()))
                .dclsEndDay(String.valueOf(product.getDclsEndDay()))
                .options(options)
                .optionCount(options.size())
                .minRate(minRate)
                .maxRate(maxRate)
                .build();
    }


    private RentHouseLoanOptionDto convertToOptionDto(RentHouseLoanOption rentHouseLoanOption) {
        if (rentHouseLoanOption == null) return null;
        return RentHouseLoanOptionDto.builder()
                .id(rentHouseLoanOption.getId())
                .dclsMonth(rentHouseLoanOption.getDclsMonth())
                .finCoNo(rentHouseLoanOption.getFinCoNo())
                .finPrdtCd(rentHouseLoanOption.getFinPrdtCd())
                .rpayType(rentHouseLoanOption.getRpayType())
                .rpayTypeNm(rentHouseLoanOption.getRpayTypeNm())
                .lendRateType(rentHouseLoanOption.getLendRateType())
                .lendRateTypeNm(rentHouseLoanOption.getLendRateTypeNm())
                .lendRateMin(rentHouseLoanOption.getLendRateMin())
                .lendRateMax(rentHouseLoanOption.getLendRateMax())
                .lendRateAvg(rentHouseLoanOption.getLendRateAvg())
                .build();
    }
}
