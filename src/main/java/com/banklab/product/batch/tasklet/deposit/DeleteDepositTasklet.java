package com.banklab.product.batch.tasklet.deposit;

import com.banklab.product.mapper.DepositOptionMapper;
import com.banklab.product.mapper.DepositProductMapper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteDepositTasklet implements Tasklet {

    @Autowired
    private DepositProductMapper depositProductMapper;
    
    @Autowired
    private DepositOptionMapper depositOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        depositProductMapper.deleteAllDepositProducts();
        depositOptionMapper.deleteAllDepositOptions();
        return RepeatStatus.FINISHED;
    }
}
