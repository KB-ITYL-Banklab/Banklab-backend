package com.banklab.product.mapper;

import com.banklab.product.domain.DepositOption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface DepositOptionMapper {
    List<DepositOption> findAllDepositOptions();
    void insertDepositOption(DepositOption option);
    void deleteAllDepositOptions();


}