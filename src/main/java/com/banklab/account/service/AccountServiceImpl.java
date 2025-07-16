package com.banklab.account.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.dto.AccountDTO;
import com.banklab.account.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Log4j2
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;


    @Override
    @Transactional
    public int saveAccounts(List<AccountVO> accountVOList) {

        int count = 0;
        for (AccountVO accountVO : accountVOList) {
            // DB 저장
            accountMapper.insertAccount(accountVO);
            count += 1;
        }

        log.info("{}개 계좌 저장", count);

        return count;
    }

    @Override
    public List<AccountDTO> getUserAccounts(String userId) {

        List<AccountVO> accounts = accountMapper.selectAccountsByUserId(userId);

        List<AccountDTO> accountDTOList = new ArrayList<>();
        for (AccountVO accountVO : accounts) {

            AccountDTO accountDTO = new AccountDTO();

            accountDTO.setResAccount(accountVO.getResAccount());
            accountDTO.setResAccountName(accountVO.getResAccountName());
            accountDTO.setResAccountDisplay(accountVO.getResAccountDisplay());
            accountDTO.setResAccountBalance(accountVO.getResAccountBalance());

            accountDTOList.add(accountDTO);
        }
        return accountDTOList;
    }
}
