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

    /**
     * CODEF API에서 불러온 계좌를 저장
     *
     * @param accountVOList : 보유 계좌 목록
     * @return 계좌 수 카운팅
     */
    public int saveAccounts(List<AccountVO> accountVOList);


    /**
     * DB에 저장된 계좌를 조회
     *
     * @param memberId : 유저 아이디
     * @return 유저의 계좌 목록
     */
    public List<AccountDTO> getUserAccounts(Long memberId);

    /**
     * API 재호출해서 잔액을 새로고침
     *
     * @param memberId : 유저아이디
     * @param bankCode : 기관코드(은행)
     * @param connectedId : 커넥티드 아이디
     * @throws Exception the exception
     */
    public void refreshAccountBalance(Long memberId, String bankCode, String connectedId) throws Exception;


    /**
     * DB에 저장된 계좌를 삭제
     *
     * @param memberId : 유저아이디
     * @param connectedId : 커넥티드 아이디
     */
    public void deleteAccount(Long memberId, String connectedId);



}
