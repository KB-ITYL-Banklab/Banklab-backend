package com.banklab.codeapi.mapper;

import com.banklab.codeapi.domain.TransactionHistoryVO;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;

public interface codeapiMapper {
    // 거래 내역 저장하기
    void insertTransactions(List<TransactionHistoryVO> list);

}
