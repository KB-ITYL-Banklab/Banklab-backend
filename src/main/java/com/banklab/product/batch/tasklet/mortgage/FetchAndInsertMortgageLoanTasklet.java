package com.banklab.product.batch.tasklet.mortgage;

import com.banklab.product.domain.deposit.DepositOption;
import com.banklab.product.domain.deposit.DepositProduct;
import com.banklab.product.dto.deposit.DepositOptionDto;
import com.banklab.product.dto.deposit.DepositProductAndOptionListDto;
import com.banklab.product.dto.deposit.DepositProductDto;
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

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
public class FetchAndInsertMortgageLoanTasklet implements Tasklet {
    
    private final MortgageLoanApiService mortgageLoanApiService;
    private final MortgageLoanProductMapper mortgageLoanProductMapper;
    private final MortgageLoanOptionMapper mortgageLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        MortgageLoanProductAndOptionListDto dto = mortgageLoanApiService.fetchProductsFromApi();

        for (MortgageLoanProductDto baseDto : dto.getProducts()) {
            MortgageLoanProduct mortgageLoanProduct = MortgageLoanProductDto.toMortgageLoanProduct(baseDto);
            mortgageLoanProductMapper.insertMortgageLoanProduct(mortgageLoanProduct);
        }

        for (MortgageLoanOptionDto optionDto : dto.getOptions()) {
            MortgageLoanOption mortgageLoanOption = MortgageLoanOptionDto.toMortgageLoanOption(optionDto);
            mortgageLoanOptionMapper.insertMortgageLoanOption(mortgageLoanOption);


        }
        return RepeatStatus.FINISHED;
    }
}
