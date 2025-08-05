package com.banklab.product.batch.tasklet.renthouse;

import com.banklab.product.mapper.RentHouseLoanProductMapper;
import com.banklab.product.mapper.RentHouseLoanOptionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class DeleteRentHouseLoanTasklet implements Tasklet {
    
    private final RentHouseLoanProductMapper rentHouseLoanProductMapper;
    private final RentHouseLoanOptionMapper rentHouseLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        rentHouseLoanProductMapper.deleteAllRentHouseLoanProduct();
        rentHouseLoanOptionMapper.deleteAllRentHouseLoanOptions();
        return RepeatStatus.FINISHED;
    }
}
