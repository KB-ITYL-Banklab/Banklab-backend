package com.banklab.product.service;

import com.banklab.product.domain.*;
import com.banklab.product.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 예금상품, 적금상품, 신용대출상품 조회
 */
@Service
public class ProductService {

    @Autowired
    private DepositProductMapper depositProductMapper;

    @Autowired
    private SavingsProductMapper savingsProductMapper;

    @Autowired
    private CreditLoanProductMapper creditLoanProductMapper;

    public List<DepositProduct> getAllDepositProducts() {
        return depositProductMapper.findAllDepositProducts();
    }

    public List<SavingsProduct> getAllSavingsProducts() {
        return savingsProductMapper.findAllSavingsProducts();
    }

    public List<CreditLoanProduct> getAllCreditLoanProducts() {
        return creditLoanProductMapper.findAllCreditLoanProducts();
    }
}
