package com.banklab.product.batch.tasklet.mortgage;

import com.banklab.product.mapper.DepositOptionMapper;
import com.banklab.product.mapper.DepositProductMapper;
import com.banklab.product.mapper.MortgageLoanProductMapper;
import com.banklab.product.mapper.MortgageLoanOptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class DeleteMortgageLoanTasklet implements Tasklet {
    
    private final MortgageLoanProductMapper mortgageLoanProductMapper;
    private final MortgageLoanOptionMapper mortgageLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        mortgageLoanProductMapper.deleteAllMortgageLoanProduct();
        mortgageLoanOptionMapper.deleteAllMortgageLoanOptions();

        return RepeatStatus.FINISHED;
    }
}
