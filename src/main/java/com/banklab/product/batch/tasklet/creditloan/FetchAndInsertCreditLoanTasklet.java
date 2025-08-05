package com.banklab.product.batch.tasklet.creditloan;

import com.banklab.product.domain.creditloan.CreditLoanOption;
import com.banklab.product.domain.creditloan.CreditLoanProduct;
import com.banklab.product.dto.creditloan.CreditLoanOptionDto;
import com.banklab.product.dto.creditloan.CreditLoanProductAndOptionListDto;
import com.banklab.product.dto.creditloan.CreditLoanProductDto;
import com.banklab.product.mapper.CreditLoanOptionMapper;
import com.banklab.product.mapper.CreditLoanProductMapper;
import com.banklab.product.service.creditloan.CreditLoanApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FetchAndInsertCreditLoanTasklet implements Tasklet {

    @Autowired
    private CreditLoanApiService creditLoanApiService;
    
    @Autowired
    private CreditLoanProductMapper creditLoanProductMapper;
    
    @Autowired
    private CreditLoanOptionMapper creditLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== 신용대출 상품 Upsert 배치 시작 ===");
        
        CreditLoanProductAndOptionListDto dto = creditLoanApiService.fetchProductsFromApi();
        
        int insertedProducts = 0;
        int updatedProducts = 0;
        int insertedOptions = 0;
        int updatedOptions = 0;

        // 상품 처리
        for (CreditLoanProductDto baseDto : dto.getProducts()) {
            CreditLoanProduct newProduct = CreditLoanProductDto.toCreditLoanProduct(baseDto);
            
            // 기존 상품 확인
            CreditLoanProduct existingProduct = creditLoanProductMapper.findByProductKey(
                newProduct.getDclsMonth(),
                newProduct.getFinCoNo(), 
                newProduct.getFinPrdtCd()
            );
            
            if (existingProduct == null) {
                // 신규 상품 삽입
                creditLoanProductMapper.insertCreditLoanProduct(newProduct);
                insertedProducts++;
                log.debug("신규 신용대출 상품 삽입: {}", newProduct.getFinPrdtNm());
            } else if (!existingProduct.getFinCoSubmDay().equals(newProduct.getFinCoSubmDay())) {
                // fin_co_subm_day가 다르면 변경된 것으로 판단하여 업데이트
                newProduct.setId(existingProduct.getId());
                creditLoanProductMapper.updateCreditLoanProduct(newProduct);
                updatedProducts++;
                log.debug("신용대출 상품 업데이트: {}", newProduct.getFinPrdtNm());
            }
        }

        // 옵션 처리
        for (CreditLoanOptionDto optionDto : dto.getOptions()) {
            CreditLoanOption newOption = CreditLoanOptionDto.toCreditLoanOption(optionDto);
            
            // 기존 옵션 확인
            CreditLoanOption existingOption = creditLoanOptionMapper.findByOptionKey(
                newOption.getDclsMonth(),
                newOption.getFinCoNo(),
                newOption.getFinPrdtCd(),
                newOption.getCrdtPrdtType(),
                newOption.getCrdtLendRateType()
            );
            
            if (existingOption == null) {
                // 신규 옵션 삽입
                creditLoanOptionMapper.insertCreditLoanOption(newOption);
                insertedOptions++;
                log.debug("신규 신용대출 옵션 삽입: {} - {}", newOption.getFinPrdtCd(), newOption.getCrdtLendRateTypeNm());
            } else if (isOptionChanged(existingOption, newOption)) {
                // 금리 정보가 변경되었으면 업데이트
                newOption.setId(existingOption.getId());
                creditLoanOptionMapper.updateCreditLoanOption(newOption);
                updatedOptions++;
                log.debug("신용대출 옵션 업데이트: {} - {}", newOption.getFinPrdtCd(), newOption.getCrdtLendRateTypeNm());
            }
        }
        
        log.info("신용대출 배치 완료 - 상품: 신규 {}, 업데이트 {} | 옵션: 신규 {}, 업데이트 {}", 
                insertedProducts, updatedProducts, insertedOptions, updatedOptions);

        return RepeatStatus.FINISHED;
    }
    
    /**
     * 옵션 변경 여부 확인 (금리 변경 체크)
     */
    private boolean isOptionChanged(CreditLoanOption existing, CreditLoanOption newOption) {
        return !safeEquals(existing.getCrdtGrad1(), newOption.getCrdtGrad1()) ||
               !safeEquals(existing.getCrdtGrad4(), newOption.getCrdtGrad4()) ||
               !safeEquals(existing.getCrdtGrad5(), newOption.getCrdtGrad5()) ||
               !safeEquals(existing.getCrdtGrad6(), newOption.getCrdtGrad6()) ||
               !safeEquals(existing.getCrdtGrad10(), newOption.getCrdtGrad10()) ||
               !safeEquals(existing.getCrdtGrad11(), newOption.getCrdtGrad11()) ||
               !safeEquals(existing.getCrdtGrad12(), newOption.getCrdtGrad12()) ||
               !safeEquals(existing.getCrdtGrad13(), newOption.getCrdtGrad13()) ||
               !safeEquals(existing.getCrdtGradAvg(), newOption.getCrdtGradAvg()) ||
               !safeEquals(existing.getCrdtLendRateTypeNm(), newOption.getCrdtLendRateTypeNm());
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
