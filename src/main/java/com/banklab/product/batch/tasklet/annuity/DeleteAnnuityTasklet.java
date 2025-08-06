package com.banklab.product.batch.tasklet.annuity;

import com.banklab.product.mapper.AnnuityOptionMapper;
import com.banklab.product.mapper.AnnuityProductMapper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteAnnuityTasklet implements Tasklet {

    @Autowired
    private AnnuityProductMapper annuityProductMapper;

    @Autowired
    private AnnuityOptionMapper annuityOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        annuityProductMapper.deleteAllAnnuityProducts();
        annuityOptionMapper.deleteAllAnnuityOptions();
        return RepeatStatus.FINISHED;
    }
}
