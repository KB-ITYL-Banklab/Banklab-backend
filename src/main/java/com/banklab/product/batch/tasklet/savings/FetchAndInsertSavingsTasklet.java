package com.banklab.product.batch.tasklet.savings;

import com.banklab.product.domain.savings.SavingsOption;
import com.banklab.product.domain.savings.SavingsProduct;
import com.banklab.product.dto.savings.SavingsOptionDto;
import com.banklab.product.dto.savings.SavingsProductAndOptionListDto;
import com.banklab.product.dto.savings.SavingsProductDto;
import com.banklab.product.mapper.SavingsOptionMapper;
import com.banklab.product.mapper.SavingsProductMapper;
import com.banklab.product.service.savings.SavingsApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FetchAndInsertSavingsTasklet implements Tasklet {

    @Autowired
    private SavingsApiService savingsApiService;
    
    @Autowired
    private SavingsProductMapper savingsProductMapper;
    
    @Autowired
    private SavingsOptionMapper savingsOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== 적금 상품 Upsert 배치 시작 ===");
        
        SavingsProductAndOptionListDto dto = savingsApiService.fetchProductsFromApi();
        
        int insertedProducts = 0;
        int updatedProducts = 0;
        int insertedOptions = 0;
        int updatedOptions = 0;

        // 상품 처리
        for (SavingsProductDto baseDto : dto.getProducts()) {
            SavingsProduct newProduct = SavingsProductDto.toSavingsProduct(baseDto);
            
            // 기존 상품 확인
            SavingsProduct existingProduct = savingsProductMapper.findByProductKey(
                newProduct.getDclsMonth(),
                newProduct.getFinCoNo(), 
                newProduct.getFinPrdtCd()
            );
            
            if (existingProduct == null) {
                // 신규 상품 삽입
                savingsProductMapper.insertSavingsProduct(newProduct);
                insertedProducts++;
                log.debug("신규 적금 상품 삽입: {}", newProduct.getFinPrdtNm());
            } else if (!existingProduct.getFinCoSubmDay().equals(newProduct.getFinCoSubmDay())) {
                // fin_co_subm_day가 다르면 변경된 것으로 판단하여 업데이트
                newProduct.setId(existingProduct.getId());
                savingsProductMapper.updateSavingsProduct(newProduct);
                updatedProducts++;
                log.debug("적금 상품 업데이트: {}", newProduct.getFinPrdtNm());
            }
        }

        // 옵션 처리
        for (SavingsOptionDto optionDto : dto.getOptions()) {
            SavingsOption newOption = SavingsOptionDto.toSavingsOption(optionDto);
            
            // 기존 옵션 확인
            SavingsOption existingOption = savingsOptionMapper.findByOptionKey(
                newOption.getDclsMonth(),
                newOption.getFinCoNo(),
                newOption.getFinPrdtCd(),
                newOption.getIntrRateType(),
                newOption.getRsrvType(),
                newOption.getSaveTrm()
            );
            
            if (existingOption == null) {
                // 신규 옵션 삽입
                savingsOptionMapper.insertSavingsOption(newOption);
                insertedOptions++;
                log.debug("신규 적금 옵션 삽입: {} - {}개월", newOption.getFinPrdtCd(), newOption.getSaveTrm());
            } else if (isOptionChanged(existingOption, newOption)) {
                // 금리 정보가 변경되었으면 업데이트
                newOption.setId(existingOption.getId());
                savingsOptionMapper.updateSavingsOption(newOption);
                updatedOptions++;
                log.debug("적금 옵션 업데이트: {} - {}개월", newOption.getFinPrdtCd(), newOption.getSaveTrm());
            }
        }
        
        log.info("적금 배치 완료 - 상품: 신규 {}, 업데이트 {} | 옵션: 신규 {}, 업데이트 {}", 
                insertedProducts, updatedProducts, insertedOptions, updatedOptions);

        return RepeatStatus.FINISHED;
    }
    
    /**
     * 옵션 변경 여부 확인
     */
    private boolean isOptionChanged(SavingsOption existing, SavingsOption newOption) {
        return !safeEquals(existing.getIntrRate(), newOption.getIntrRate()) ||
               !safeEquals(existing.getIntrRate2(), newOption.getIntrRate2()) ||
               !safeEquals(existing.getIntrRateTypeNm(), newOption.getIntrRateTypeNm()) ||
               !safeEquals(existing.getRsrvTypeNm(), newOption.getRsrvTypeNm());
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
