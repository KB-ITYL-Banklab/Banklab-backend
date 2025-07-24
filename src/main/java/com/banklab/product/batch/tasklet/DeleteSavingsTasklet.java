package com.banklab.product.batch.tasklet;

import com.banklab.product.mapper.DepositOptionMapper;
import com.banklab.product.mapper.DepositProductMapper;
import com.banklab.product.mapper.SavingsOptionMapper;
import com.banklab.product.mapper.SavingsProductMapper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteSavingsTasklet implements Tasklet {

    @Autowired
    private SavingsProductMapper savingsProductMapper;
    @Autowired
    private SavingsOptionMapper savingsOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        savingsProductMapper.deleteAllSavingsProducts();
        savingsOptionMapper.deleteAllSavingsOptions();
        return RepeatStatus.FINISHED;
    }
}

