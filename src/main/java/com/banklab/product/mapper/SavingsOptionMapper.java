package com.banklab.product.mapper;

import com.banklab.product.domain.DepositOption;
import com.banklab.product.domain.SavingsOption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SavingsOptionMapper {
    List<SavingsOption> findAllSavingsOptions();
    void insertSavingsOption(SavingsOption option);
    void deleteAllSavingsOptions();
}
