package com.banklab.product.batch.tasklet.savings;

import com.banklab.product.domain.savings.SavingsOption;
import com.banklab.product.domain.savings.SavingsProduct;
import com.banklab.product.dto.savings.SavingsOptionDto;
import com.banklab.product.dto.savings.SavingsProductAndOptionListDto;
import com.banklab.product.dto.savings.SavingsProductDto;
import com.banklab.product.mapper.SavingsOptionMapper;
import com.banklab.product.mapper.SavingsProductMapper;
import com.banklab.product.service.savings.SavingsApiService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FetchAndInsertSavingsTasklet implements Tasklet {

    @Autowired
    private SavingsApiService savingsApiService;
    
    @Autowired
    private SavingsProductMapper savingsProductMapper;
    
    @Autowired
    private SavingsOptionMapper savingsOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        SavingsProductAndOptionListDto dto = savingsApiService.fetchProductsFromApi();

        for (SavingsProductDto baseDto : dto.getProducts()) {
            SavingsProduct savingsProduct = SavingsProductDto.toSavingsProduct(baseDto);
            savingsProductMapper.insertSavingsProduct(savingsProduct);
        }

        for (SavingsOptionDto optionDto : dto.getOptions()) {
            SavingsOption savingsOption = SavingsOptionDto.toSavingsOption(optionDto);
            savingsOptionMapper.insertSavingsOption(savingsOption);
        }

        return RepeatStatus.FINISHED;
    }
}
