package com.banklab.account.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 계좌 서비스 인터페이스
 */
public interface AccountService {

    // 계좌 저장
    public int saveAccounts(List<AccountVO> accountVOList);

    // 계좌 조회
    List<AccountDTO> getUserAccounts(String userId);






}
