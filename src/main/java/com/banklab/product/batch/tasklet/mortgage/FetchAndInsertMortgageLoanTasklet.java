package com.banklab.product.batch.tasklet.mortgage;

import com.banklab.product.dto.mortgage.MortgageLoanProductAndOptionListDto;
import com.banklab.product.dto.mortgage.MortgageLoanProductDto;
import com.banklab.product.dto.mortgage.MortgageLoanOptionDto;
import com.banklab.product.domain.mortgage.MortgageLoanProduct;
import com.banklab.product.domain.mortgage.MortgageLoanOption;
import com.banklab.product.mapper.MortgageLoanProductMapper;
import com.banklab.product.mapper.MortgageLoanOptionMapper;
import com.banklab.product.service.mortgage.MortgageLoanApiService;
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
public class FetchAndInsertMortgageLoanTasklet implements Tasklet {
    
    private final MortgageLoanApiService mortgageLoanApiService;
    private final MortgageLoanProductMapper mortgageLoanProductMapper;
    private final MortgageLoanOptionMapper mortgageLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== 주택담보대출 상품 Upsert 배치 시작 ===");
        
        MortgageLoanProductAndOptionListDto dto = mortgageLoanApiService.fetchProductsFromApi();
        
        int insertedProducts = 0;
        int updatedProducts = 0;
        int insertedOptions = 0;
        int updatedOptions = 0;

        // 상품 처리
        for (MortgageLoanProductDto baseDto : dto.getProducts()) {
            MortgageLoanProduct newProduct = MortgageLoanProductDto.toMortgageLoanProduct(baseDto);
            
            // 기존 상품 확인
            MortgageLoanProduct existingProduct = mortgageLoanProductMapper.findByProductKey(
                newProduct.getDclsMonth(),
                newProduct.getFinCoNo(), 
                newProduct.getFinPrdtCd()
            );
            
            if (existingProduct == null) {
                // 신규 상품 삽입
                mortgageLoanProductMapper.insertMortgageLoanProduct(newProduct);
                insertedProducts++;
                log.debug("신규 주택담보대출 상품 삽입: {}", newProduct.getFinPrdtNm());
            } else if (!existingProduct.getFinCoSubmDay().equals(newProduct.getFinCoSubmDay())) {
                // fin_co_subm_day가 다르면 변경된 것으로 판단하여 업데이트
                newProduct.setId(existingProduct.getId());
                mortgageLoanProductMapper.updateMortgageLoanProduct(newProduct);
                updatedProducts++;
                log.debug("주택담보대출 상품 업데이트: {}", newProduct.getFinPrdtNm());
            }
        }

        // 옵션 처리
        for (MortgageLoanOptionDto optionDto : dto.getOptions()) {
            MortgageLoanOption newOption = MortgageLoanOptionDto.toMortgageLoanOption(optionDto);
            
            // 기존 옵션 확인
            MortgageLoanOption existingOption = mortgageLoanOptionMapper.findByOptionKey(
                newOption.getDclsMonth(),
                newOption.getFinCoNo(),
                newOption.getFinPrdtCd(),
                newOption.getMrtgType(),
                newOption.getRpayType(),
                newOption.getLendRateType()
            );
            
            if (existingOption == null) {
                // 신규 옵션 삽입
                mortgageLoanOptionMapper.insertMortgageLoanOption(newOption);
                insertedOptions++;
                log.debug("신규 주택담보대출 옵션 삽입: {} - {}", newOption.getFinPrdtCd(), newOption.getLendRateTypeNm());
            } else if (isOptionChanged(existingOption, newOption)) {
                // 금리 정보가 변경되었으면 업데이트
                newOption.setId(existingOption.getId());
                mortgageLoanOptionMapper.updateMortgageLoanOption(newOption);
                updatedOptions++;
                log.debug("주택담보대출 옵션 업데이트: {} - {}", newOption.getFinPrdtCd(), newOption.getLendRateTypeNm());
            }
        }
        
        log.info("주택담보대출 배치 완료 - 상품: 신규 {}, 업데이트 {} | 옵션: 신규 {}, 업데이트 {}", 
                insertedProducts, updatedProducts, insertedOptions, updatedOptions);

        return RepeatStatus.FINISHED;
    }
    
    /**
     * 옵션 변경 여부 확인 (금리 변경 체크)
     */
    private boolean isOptionChanged(MortgageLoanOption existing, MortgageLoanOption newOption) {
        return !safeEquals(existing.getLendRateMin(), newOption.getLendRateMin()) ||
               !safeEquals(existing.getLendRateMax(), newOption.getLendRateMax()) ||
               !safeEquals(existing.getLendRateAvg(), newOption.getLendRateAvg()) ||
               !safeEquals(existing.getMrtgTypeNm(), newOption.getMrtgTypeNm()) ||
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
