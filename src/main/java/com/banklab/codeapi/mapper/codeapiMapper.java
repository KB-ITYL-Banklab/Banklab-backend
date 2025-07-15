package com.banklab.codeapi.mapper;

import com.banklab.codeapi.domain.TransactionHistoryVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface codeapiMapper {

    // 거래 내역 불러오기
    List<TransactionHistoryVO> getTransactionList();
    
    // 거래 내역 저장하기
    void insertTransactions(List<TransactionHistoryVO> list);

}
