package com.banklab.product.batch.tasklet.annuity;

import com.banklab.product.domain.annuity.AnnuityOption;
import com.banklab.product.domain.annuity.AnnuityProduct;
import com.banklab.product.dto.annuity.AnnuityOptionDto;
import com.banklab.product.dto.annuity.AnnuityProductAndOptionListDto;
import com.banklab.product.dto.annuity.AnnuityProductDto;
import com.banklab.product.mapper.AnnuityOptionMapper;
import com.banklab.product.mapper.AnnuityProductMapper;
import com.banklab.product.service.annuity.AnnuityApiService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FetchAndInsertAnnuityTasklet implements Tasklet {

    @Autowired
    private AnnuityApiService annuityApiService;

    @Autowired
    private AnnuityProductMapper annuityProductMapper;

    @Autowired
    private AnnuityOptionMapper annuityOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        AnnuityProductAndOptionListDto dto = annuityApiService.fetchProductsFromApi();

        for (AnnuityProductDto baseDto : dto.getProducts()){
            AnnuityProduct annuityProduct = AnnuityProductDto.toAnnuityProduct(baseDto);
            annuityProductMapper.insertAnnuityProduct(annuityProduct);
        }

        for (AnnuityOptionDto optionDto : dto.getOptions()){
            AnnuityOption annuityOption = AnnuityOptionDto.toAnnuityOption(optionDto);
            annuityOptionMapper.insertAnnuityOption(annuityOption);
        }

        return RepeatStatus.FINISHED;
    }
}
