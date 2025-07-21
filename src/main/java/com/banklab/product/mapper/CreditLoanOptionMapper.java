package com.banklab.product.mapper;

import com.banklab.product.domain.CreditLoanOption;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CreditLoanOptionMapper {
    List<CreditLoanOption> findAllCreditLoanOptions();
    void insertCreditLoanOption(CreditLoanOption option);
    void deleteAllCreditLoanOptions();

}
