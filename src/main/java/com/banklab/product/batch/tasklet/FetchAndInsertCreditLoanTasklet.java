package com.banklab.product.batch.tasklet;

import com.banklab.product.domain.CreditLoanOption;
import com.banklab.product.domain.CreditLoanProduct;
import com.banklab.product.dto.creditloan.CreditLoanOptionDto;
import com.banklab.product.dto.creditloan.CreditLoanProductAndOptionListDto;
import com.banklab.product.dto.creditloan.CreditLoanProductDto;
import com.banklab.product.mapper.CreditLoanOptionMapper;
import com.banklab.product.mapper.CreditLoanProductMapper;
import com.banklab.product.service.CreditLoanApiService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FetchAndInsertCreditLoanTasklet implements Tasklet {

    @Autowired
    private CreditLoanApiService creditLoanApiService;
    
    @Autowired
    private CreditLoanProductMapper creditLoanProductMapper;
    
    @Autowired
    private CreditLoanOptionMapper creditLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        CreditLoanProductAndOptionListDto dto = creditLoanApiService.fetchProductsFromApi();

        for (CreditLoanProductDto baseDto : dto.getProducts()) {
            CreditLoanProduct creditLoanProduct = CreditLoanProductDto.toCreditLoanProduct(baseDto);
            creditLoanProductMapper.insertCreditLoanProduct(creditLoanProduct);
        }

        for (CreditLoanOptionDto optionDto : dto.getOptions()) {
            CreditLoanOption creditLoanOption = CreditLoanOptionDto.toCreditLoanOption(optionDto);
            creditLoanOptionMapper.insertCreditLoanOption(creditLoanOption);
        }

        return RepeatStatus.FINISHED;
    }
}
