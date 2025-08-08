package com.banklab.product.batch.tasklet.annuity;

import com.banklab.product.domain.annuity.AnnuityOption;
import com.banklab.product.domain.annuity.AnnuityProduct;
import com.banklab.product.dto.annuity.AnnuityOptionDto;
import com.banklab.product.dto.annuity.AnnuityProductAndOptionListDto;
import com.banklab.product.dto.annuity.AnnuityProductDto;
import com.banklab.product.mapper.AnnuityOptionMapper;
import com.banklab.product.mapper.AnnuityProductMapper;
import com.banklab.product.service.annuity.AnnuityApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FetchAndInsertAnnuityTasklet implements Tasklet {

    @Autowired
    private AnnuityApiService annuityApiService;

    @Autowired
    private AnnuityProductMapper annuityProductMapper;

    @Autowired
    private AnnuityOptionMapper annuityOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("=== 연금저축 상품 Upsert 배치 시작 ===");
        
        AnnuityProductAndOptionListDto dto = annuityApiService.fetchProductsFromApi();
        
        int insertedProducts = 0;
        int updatedProducts = 0;
        int insertedOptions = 0;
        int updatedOptions = 0;

        // 상품 처리
        for (AnnuityProductDto baseDto : dto.getProducts()) {
            AnnuityProduct newProduct = AnnuityProductDto.toAnnuityProduct(baseDto);
            
            // 기존 상품 확인
            AnnuityProduct existingProduct = annuityProductMapper.findByProductKey(
                newProduct.getDclsMonth(),
                newProduct.getFinCoNo(), 
                newProduct.getFinPrdtCd()
            );
            
            if (existingProduct == null) {
                // 신규 상품 삽입
                annuityProductMapper.insertAnnuityProduct(newProduct);
                insertedProducts++;
                log.debug("신규 연금저축 상품 삽입: {}", newProduct.getFinPrdtNm());
            } else if (!existingProduct.getFinCoSubmDay().equals(newProduct.getFinCoSubmDay())) {
                // fin_co_subm_day가 다르면 변경된 것으로 판단하여 업데이트
                newProduct.setId(existingProduct.getId());
                annuityProductMapper.updateAnnuityProduct(newProduct);
                updatedProducts++;
                log.debug("연금저축 상품 업데이트: {}", newProduct.getFinPrdtNm());
            }
        }

        // 옵션 처리
        for (AnnuityOptionDto optionDto : dto.getOptions()) {
            AnnuityOption newOption = AnnuityOptionDto.toAnnuityOption(optionDto);
            
            // 기존 옵션 확인
            AnnuityOption existingOption = annuityOptionMapper.findByOptionKey(
                newOption.getDclsMonth(),
                newOption.getFinCoNo(),
                newOption.getFinPrdtCd(),
                newOption.getPnsnRecpTrm(),
                newOption.getPnsnEntrAge(),
                newOption.getMonPaymAtm(),
                newOption.getPaymPrd(),
                newOption.getPnsnStrtAge()
            );
            
            if (existingOption == null) {
                // 신규 옵션 삽입
                annuityOptionMapper.insertAnnuityOption(newOption);
                insertedOptions++;
                log.debug("신규 연금저축 옵션 삽입: {} - {}", newOption.getFinPrdtCd(), newOption.getPnsnRecpTrmNm());
            } else if (isOptionChanged(existingOption, newOption)) {
                // 연금 정보가 변경되었으면 업데이트
                newOption.setId(existingOption.getId());
                annuityOptionMapper.updateAnnuityOption(newOption);
                updatedOptions++;
                log.debug("연금저축 옵션 업데이트: {} - {}", newOption.getFinPrdtCd(), newOption.getPnsnRecpTrmNm());
            }
        }
        
        log.info("연금저축 배치 완료 - 상품: 신규 {}, 업데이트 {} | 옵션: 신규 {}, 업데이트 {}", 
                insertedProducts, updatedProducts, insertedOptions, updatedOptions);

        return RepeatStatus.FINISHED;
    }
    
    /**
     * 옵션 변경 여부 확인
     */
    private boolean isOptionChanged(AnnuityOption existing, AnnuityOption newOption) {
        return !safeEquals(existing.getPnsnRecpAmt(), newOption.getPnsnRecpAmt()) ||
               !safeEquals(existing.getPnsnRecpTrmNm(), newOption.getPnsnRecpTrmNm()) ||
               !safeEquals(existing.getPnsnEntrAgeNm(), newOption.getPnsnEntrAgeNm()) ||
               !safeEquals(existing.getMonPaymAtmNm(), newOption.getMonPaymAtmNm()) ||
               !safeEquals(existing.getPaymPrdNm(), newOption.getPaymPrdNm()) ||
               !safeEquals(existing.getPnsnStrtAgeNm(), newOption.getPnsnStrtAgeNm());
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
