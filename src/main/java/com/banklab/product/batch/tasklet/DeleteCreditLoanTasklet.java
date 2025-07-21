package com.banklab.product.batch.tasklet;

import com.banklab.product.mapper.CreditLoanOptionMapper;
import com.banklab.product.mapper.CreditLoanProductMapper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteCreditLoanTasklet implements Tasklet {

    @Autowired
    private CreditLoanProductMapper creditLoanProductMapper;
    
    @Autowired
    private CreditLoanOptionMapper creditLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        creditLoanProductMapper.deleteAllCreditLoanProducts();
        creditLoanOptionMapper.deleteAllCreditLoanOptions();
        return RepeatStatus.FINISHED;
    }
}
