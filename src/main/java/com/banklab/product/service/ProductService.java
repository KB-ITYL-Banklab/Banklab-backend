package com.banklab.product.service;

import com.banklab.product.domain.*;
import com.banklab.product.dto.creditloan.CreditLoanOptionDto;
import com.banklab.product.dto.creditloan.CreditLoanProductAndOptionListDto;
import com.banklab.product.dto.creditloan.CreditLoanProductDto;
import com.banklab.product.dto.deposit.DepositOptionDto;
import com.banklab.product.dto.deposit.DepositProductAndOptionListDto;
import com.banklab.product.dto.deposit.DepositProductDto;
import com.banklab.product.dto.savings.SavingsOptionDto;
import com.banklab.product.dto.savings.SavingsProductAndOptionListDto;
import com.banklab.product.dto.savings.SavingsProductDto;
import com.banklab.product.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    // Deposit related
    @Autowired
    private DepositProductMapper depositProductMapper;
    @Autowired
    private DepositOptionMapper depositOptionMapper;
    @Autowired
    private DepositApiService depositApiService;

    // Savings related
    @Autowired
    private SavingsProductMapper savingsProductMapper;
    @Autowired
    private SavingsOptionMapper savingsOptionMapper;
    @Autowired
    private SavingsApiService savingsApiService;

    // Credit Loan related
    @Autowired
    private CreditLoanProductMapper creditLoanProductMapper;
    @Autowired
    private CreditLoanOptionMapper creditLoanOptionMapper;
    @Autowired
    private CreditLoanApiService creditLoanApiService;

    // ===== DEPOSIT METHODS =====
    public void refreshDepositProducts() {
        DepositProductAndOptionListDto response = depositApiService.fetchProductsFromApi();

        if (response != null) {
            List<DepositProductDto> products = response.getProducts();
            List<DepositOptionDto> options = response.getOptions();

            // 기존 데이터 삭제
            depositProductMapper.deleteAllDepositProducts();
            depositOptionMapper.deleteAllDepositOptions();

            // 새로운 데이터 삽입
            for (DepositProductDto productDto : products) {
                DepositProduct product = DepositProductDto.toDepositProduct(productDto);
                depositProductMapper.insertDepositProduct(product);
            }

            for (DepositOptionDto optionDto : options) {
                DepositOption option = DepositOptionDto.toDepositOption(optionDto);
                depositOptionMapper.insertDepositOption(option);
            }
        }
    }

    public List<DepositProduct> getAllDepositProducts() {
        return depositProductMapper.findAllDepositProducts();
    }

    public List<DepositOption> getAllDepositOptions() {
        return depositOptionMapper.findAllDepositOptions();
    }

    // ===== SAVINGS METHODS =====
    public void refreshSavingsProducts() {
        SavingsProductAndOptionListDto response = savingsApiService.fetchProductsFromApi();

        if (response != null) {
            List<SavingsProductDto> products = response.getProducts();
            List<SavingsOptionDto> options = response.getOptions();

            // 기존 데이터 삭제
            savingsProductMapper.deleteAllSavingsProducts();
            savingsOptionMapper.deleteAllSavingsOptions();

            // 새로운 데이터 삽입
            for (SavingsProductDto productDto : products) {
                SavingsProduct product = SavingsProductDto.toSavingsProduct(productDto);
                savingsProductMapper.insertSavingsProduct(product);
            }

            for (SavingsOptionDto optionDto : options) {
                SavingsOption option = SavingsOptionDto.toSavingsOption(optionDto);
                savingsOptionMapper.insertSavingsOption(option);
            }
        }
    }

    public List<SavingsProduct> getAllSavingsProducts() {
        return savingsProductMapper.findAllSavingsProducts();
    }

    public List<SavingsOption> getAllSavingsOptions() {
        return savingsOptionMapper.findAllSavingsOptions();
    }

    // ===== CREDIT LOAN METHODS =====
    public void refreshCreditLoanProducts() {
        CreditLoanProductAndOptionListDto response = creditLoanApiService.fetchProductsFromApi();

        if (response != null) {
            List<CreditLoanProductDto> products = response.getProducts();
            List<CreditLoanOptionDto> options = response.getOptions();

            // 기존 데이터 삭제
            creditLoanProductMapper.deleteAllCreditLoanProducts();
            creditLoanOptionMapper.deleteAllCreditLoanOptions();

            // 새로운 데이터 삽입
            for (CreditLoanProductDto productDto : products) {
                CreditLoanProduct product = CreditLoanProductDto.toCreditLoanProduct(productDto);
                creditLoanProductMapper.insertCreditLoanProduct(product);
            }

            for (CreditLoanOptionDto optionDto : options) {
                CreditLoanOption option = CreditLoanOptionDto.toCreditLoanOption(optionDto);
                creditLoanOptionMapper.insertCreditLoanOption(option);
            }
        }
    }

    public List<CreditLoanProduct> getAllCreditLoanProducts() {
        return creditLoanProductMapper.findAllCreditLoanProducts();
    }

    public List<CreditLoanOption> getAllCreditLoanOptions() {
        return creditLoanOptionMapper.findAllCreditLoanOptions();
    }

    // ===== UTILITY METHODS =====
    public void refreshAllProducts() {
        System.out.println("모든 금융상품 데이터 갱신 시작...");
        
        System.out.println("예금 상품 갱신 중...");
        refreshDepositProducts();
        
        System.out.println("적금 상품 갱신 중...");
        refreshSavingsProducts();
        
        System.out.println("신용대출 상품 갱신 중...");
        refreshCreditLoanProducts();
        
        System.out.println("모든 금융상품 데이터 갱신 완료");
    }

    public void deleteAllData() {
        depositProductMapper.deleteAllDepositProducts();
        depositOptionMapper.deleteAllDepositOptions();
        savingsProductMapper.deleteAllSavingsProducts();
        savingsOptionMapper.deleteAllSavingsOptions();
        creditLoanProductMapper.deleteAllCreditLoanProducts();
        creditLoanOptionMapper.deleteAllCreditLoanOptions();
    }
}
