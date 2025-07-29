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
    public List<AccountDTO> getUserAccounts(Long memberId) {

        List<AccountVO> accounts = accountMapper.selectAccountsByUserId(memberId);

        List<AccountDTO> accountDTOList = new ArrayList<>();
        for (AccountVO accountVO : accounts) {

            AccountDTO accountDTO = new AccountDTO();

            accountDTO.setId(accountVO.getId()); // ID 필드 추가
            accountDTO.setResAccount(accountVO.getResAccount());
            accountDTO.setResAccountName(accountVO.getResAccountName());
            accountDTO.setResAccountDisplay(accountVO.getResAccountDisplay());
            accountDTO.setResAccountBalance(accountVO.getResAccountBalance());

            accountDTO.setConnectedId(accountVO.getConnectedId());
            accountDTO.setOrganization(accountVO.getOrganization());

            accountDTOList.add(accountDTO);
        }
        return accountDTOList;
    }

    @Override
    public void refreshAccountBalance(Long memberId, String bankCode, String connectedId) throws Exception {

        List<AccountVO> latestAccounts = AccountResponse.requestAccounts(Long.valueOf(memberId), bankCode, connectedId);

        if (latestAccounts.isEmpty()) {
            log.warn("API에서 계좌 정보를 가져오지 못했습니다.");
            return;
        }

        for (AccountVO accountVO : latestAccounts) {
            String resAccount = accountVO.getResAccount();
            String newBalance = accountVO.getResAccountBalance();

            accountMapper.updateAccountBalance(memberId, resAccount, newBalance);

        }
    }

    @Override
    public void deleteAccount(Long memberId, String connectedId) {
        accountMapper.deleteAccount(memberId, connectedId);
    }

    @Override
    public boolean isConnectedIdOwner(Long memberId, String connectedId) {
        // DB에서 해당 memberId와 connectedId로 계좌가 존재하는지 확인
        List<AccountVO> accounts = accountMapper.selectAccountsByUserId(memberId);

        return accounts.stream()
                .anyMatch(account -> connectedId.equals(account.getConnectedId()));
    }
}
