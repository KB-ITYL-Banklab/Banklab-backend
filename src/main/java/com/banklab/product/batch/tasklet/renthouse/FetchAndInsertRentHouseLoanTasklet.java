package com.banklab.product.batch.tasklet.renthouse;

import com.banklab.product.dto.renthouse.RentHouseLoanProductAndOptionListDto;
import com.banklab.product.dto.renthouse.RentHouseLoanProductDto;
import com.banklab.product.dto.renthouse.RentHouseLoanOptionDto;
import com.banklab.product.domain.renthouse.RentHouseLoanProduct;
import com.banklab.product.domain.renthouse.RentHouseLoanOption;
import com.banklab.product.mapper.RentHouseLoanProductMapper;
import com.banklab.product.mapper.RentHouseLoanOptionMapper;
import com.banklab.product.service.renthouse.RentHouseLoanApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class FetchAndInsertRentHouseLoanTasklet implements Tasklet {
    
    private final RentHouseLoanApiService rentHouseLoanApiService;
    private final RentHouseLoanProductMapper rentHouseLoanProductMapper;
    private final RentHouseLoanOptionMapper rentHouseLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== 전세자금대출 상품 Upsert 배치 시작 ===");
        
        RentHouseLoanProductAndOptionListDto dto = rentHouseLoanApiService.fetchProductsFromApi();
        
        int insertedProducts = 0;
        int updatedProducts = 0;
        int insertedOptions = 0;
        int updatedOptions = 0;

        // 상품 처리
        for (RentHouseLoanProductDto baseDto : dto.getProducts()) {
            RentHouseLoanProduct newProduct = RentHouseLoanProductDto.toRentHouseLoanProduct(baseDto);
            
            // 기존 상품 확인
            RentHouseLoanProduct existingProduct = rentHouseLoanProductMapper.findByProductKey(
                newProduct.getDclsMonth(),
                newProduct.getFinCoNo(), 
                newProduct.getFinPrdtCd()
            );
            
            if (existingProduct == null) {
                // 신규 상품 삽입
                rentHouseLoanProductMapper.insertRentHouseLoanProduct(newProduct);
                insertedProducts++;
                log.debug("신규 전세자금대출 상품 삽입: {}", newProduct.getFinPrdtNm());
            } else if (!existingProduct.getFinCoSubmDay().equals(newProduct.getFinCoSubmDay())) {
                // fin_co_subm_day가 다르면 변경된 것으로 판단하여 업데이트
                newProduct.setId(existingProduct.getId());
                rentHouseLoanProductMapper.updateRentHouseLoanProduct(newProduct);
                updatedProducts++;
                log.debug("전세자금대출 상품 업데이트: {}", newProduct.getFinPrdtNm());
            }
        }

        // 옵션 처리
        for (RentHouseLoanOptionDto optionDto : dto.getOptions()) {
            RentHouseLoanOption newOption = RentHouseLoanOptionDto.toRentHouseLoanOption(optionDto);
            
            // 기존 옵션 확인
            RentHouseLoanOption existingOption = rentHouseLoanOptionMapper.findByOptionKey(
                newOption.getDclsMonth(),
                newOption.getFinCoNo(),
                newOption.getFinPrdtCd(),
                newOption.getRpayType(),
                newOption.getLendRateType()
            );
            
            if (existingOption == null) {
                // 신규 옵션 삽입
                rentHouseLoanOptionMapper.insertRentHouseLoanOption(newOption);
                insertedOptions++;
                log.debug("신규 전세자금대출 옵션 삽입: {} - {}", newOption.getFinPrdtCd(), newOption.getLendRateTypeNm());
            } else if (isOptionChanged(existingOption, newOption)) {
                // 금리 정보가 변경되었으면 업데이트
                newOption.setId(existingOption.getId());
                rentHouseLoanOptionMapper.updateRentHouseLoanOption(newOption);
                updatedOptions++;
                log.debug("전세자금대출 옵션 업데이트: {} - {}", newOption.getFinPrdtCd(), newOption.getLendRateTypeNm());
            }
        }
        
        log.info("전세자금대출 배치 완료 - 상품: 신규 {}, 업데이트 {} | 옵션: 신규 {}, 업데이트 {}", 
                insertedProducts, updatedProducts, insertedOptions, updatedOptions);

        return RepeatStatus.FINISHED;
    }
    
    /**
     * 옵션 변경 여부 확인 (금리 변경 체크)
     */
    private boolean isOptionChanged(RentHouseLoanOption existing, RentHouseLoanOption newOption) {
        return !safeEquals(existing.getLendRateMin(), newOption.getLendRateMin()) ||
               !safeEquals(existing.getLendRateMax(), newOption.getLendRateMax()) ||
               !safeEquals(existing.getLendRateAvg(), newOption.getLendRateAvg()) ||
               !safeEquals(existing.getRpayTypeNm(), newOption.getRpayTypeNm()) ||
               !safeEquals(existing.getLendRateTypeNm(), newOption.getLendRateTypeNm());
    }
    
    /**
     * null-safe equals 비교
     */
    private boolean safeEquals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == null || obj2 == null) return false;
        return obj1.equals(obj2);
    }
}
