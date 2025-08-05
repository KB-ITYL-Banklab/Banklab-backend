package com.banklab.product.batch.tasklet.renthouse;

import com.banklab.product.dto.renthouse.RentHouseLoanProductAndOptionListDto;
import com.banklab.product.dto.renthouse.RentHouseLoanProductDto;
import com.banklab.product.dto.renthouse.RentHouseLoanOptionDto;
import com.banklab.product.domain.renthouse.RentHouseLoanProduct;
import com.banklab.product.domain.renthouse.RentHouseLoanOption;
import com.banklab.product.mapper.RentHouseLoanProductMapper;
import com.banklab.product.mapper.RentHouseLoanOptionMapper;
import com.banklab.product.service.renthouse.RentHouseLoanApiService;
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
public class FetchAndInsertRentHouseLoanTasklet implements Tasklet {
    
    private final RentHouseLoanApiService rentHouseLoanApiService;
    private final RentHouseLoanProductMapper rentHouseLoanProductMapper;
    private final RentHouseLoanOptionMapper rentHouseLoanOptionMapper;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        
        RentHouseLoanProductAndOptionListDto dto = rentHouseLoanApiService.fetchProductsFromApi();

        for (RentHouseLoanProductDto baseDto : dto.getProducts()) {
            RentHouseLoanProduct rentHouseLoanProduct = RentHouseLoanProductDto.toRentHouseLoanProduct(baseDto);
            rentHouseLoanProductMapper.insertRentHouseLoanProduct(rentHouseLoanProduct);
        }

        for (RentHouseLoanOptionDto optionDto : dto.getOptions()) {
            RentHouseLoanOption rentHouseLoanOption = RentHouseLoanOptionDto.toRentHouseLoanOption(optionDto);
            rentHouseLoanOptionMapper.insertRentHouseLoanOption(rentHouseLoanOption);
        }

        return RepeatStatus.FINISHED;
    }
}
