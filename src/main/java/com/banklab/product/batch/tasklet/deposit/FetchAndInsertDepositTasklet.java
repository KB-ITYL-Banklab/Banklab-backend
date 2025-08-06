package com.banklab.product.batch.tasklet.deposit;

import com.banklab.product.domain.deposit.DepositOption;
import com.banklab.product.domain.deposit.DepositProduct;
import com.banklab.product.dto.deposit.DepositOptionDto;
import com.banklab.product.dto.deposit.DepositProductAndOptionListDto;
import com.banklab.product.dto.deposit.DepositProductDto;
import com.banklab.product.mapper.DepositOptionMapper;
import com.banklab.product.mapper.DepositProductMapper;
import com.banklab.product.service.deposit.DepositApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FetchAndInsertDepositTasklet implements Tasklet {

    @Autowired
    private DepositApiService depositApiService;
    
    @Autowired
    private DepositProductMapper depositProductMapper;
    
    @Autowired
    private DepositOptionMapper depositOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== 예금 상품 Upsert 배치 시작 ===");
        
        DepositProductAndOptionListDto dto = depositApiService.fetchProductsFromApi();
        
        int insertedProducts = 0;
        int updatedProducts = 0;
        int insertedOptions = 0;
        int updatedOptions = 0;

        // 상품 처리
        for (DepositProductDto baseDto : dto.getProducts()) {
            DepositProduct newProduct = DepositProductDto.toDepositProduct(baseDto);
            
            // 기존 상품 확인
            DepositProduct existingProduct = depositProductMapper.findByProductKey(
                newProduct.getDclsMonth(),
                newProduct.getFinCoNo(), 
                newProduct.getFinPrdtCd()
            );
            
            if (existingProduct == null) {
                // 신규 상품 삽입
                depositProductMapper.insertDepositProduct(newProduct);
                insertedProducts++;
                log.debug("신규 예금 상품 삽입: {}", newProduct.getFinPrdtNm());
            } else if (!existingProduct.getFinCoSubmDay().equals(newProduct.getFinCoSubmDay())) {
                // fin_co_subm_day가 다르면 변경된 것으로 판단하여 업데이트
                newProduct.setId(existingProduct.getId());
                depositProductMapper.updateDepositProduct(newProduct);
                updatedProducts++;
                log.debug("예금 상품 업데이트: {}", newProduct.getFinPrdtNm());
            }
            // fin_co_subm_day가 같으면 변경 없음으로 판단하여 그대로 둠
        }

        // 옵션 처리
        for (DepositOptionDto optionDto : dto.getOptions()) {
            DepositOption newOption = DepositOptionDto.toDepositOption(optionDto);
            
            // 기존 옵션 확인
            DepositOption existingOption = depositOptionMapper.findByOptionKey(
                newOption.getDclsMonth(),
                newOption.getFinCoNo(),
                newOption.getFinPrdtCd(),
                newOption.getIntrRateType(),
                newOption.getSaveTrm()
            );
            
            if (existingOption == null) {
                // 신규 옵션 삽입
                depositOptionMapper.insertDepositOption(newOption);
                insertedOptions++;
                log.debug("신규 예금 옵션 삽입: {} - {}개월", newOption.getFinPrdtCd(), newOption.getSaveTrm());
            } else if (isOptionChanged(existingOption, newOption)) {
                // 금리 정보가 변경되었으면 업데이트
                newOption.setId(existingOption.getId());
                depositOptionMapper.updateDepositOption(newOption);
                updatedOptions++;
                log.debug("예금 옵션 업데이트: {} - {}개월", newOption.getFinPrdtCd(), newOption.getSaveTrm());
            }
        }
        
        log.info("예금 배치 완료 - 상품: 신규 {}, 업데이트 {} | 옵션: 신규 {}, 업데이트 {}", 
                insertedProducts, updatedProducts, insertedOptions, updatedOptions);

        return RepeatStatus.FINISHED;
    }
    
    /**
     * 옵션 변경 여부 확인
     */
    private boolean isOptionChanged(DepositOption existing, DepositOption newOption) {
        return !safeEquals(existing.getIntrRate(), newOption.getIntrRate()) ||
               !safeEquals(existing.getIntrRate2(), newOption.getIntrRate2()) ||
               !safeEquals(existing.getIntrRateTypeNm(), newOption.getIntrRateTypeNm());
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
