package com.banklab.product.batch.tasklet.deposit;

import com.banklab.product.domain.deposit.DepositOption;
import com.banklab.product.domain.deposit.DepositProduct;
import com.banklab.product.dto.deposit.DepositOptionDto;
import com.banklab.product.dto.deposit.DepositProductAndOptionListDto;
import com.banklab.product.dto.deposit.DepositProductDto;
import com.banklab.product.mapper.DepositOptionMapper;
import com.banklab.product.mapper.DepositProductMapper;
import com.banklab.product.service.deposit.DepositApiService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FetchAndInsertDepositTasklet implements Tasklet {

    @Autowired
    private DepositApiService depositApiService;
    
    @Autowired
    private DepositProductMapper depositProductMapper;
    
    @Autowired
    private DepositOptionMapper depositOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        DepositProductAndOptionListDto dto = depositApiService.fetchProductsFromApi();

        for (DepositProductDto baseDto : dto.getProducts()) {
            DepositProduct depositProduct = DepositProductDto.toDepositProduct(baseDto);
            depositProductMapper.insertDepositProduct(depositProduct);
        }

        for (DepositOptionDto optionDto : dto.getOptions()) {
            DepositOption depositOption = DepositOptionDto.toDepositOption(optionDto);
            depositOptionMapper.insertDepositOption(depositOption);
        }

        return RepeatStatus.FINISHED;
    }
}
