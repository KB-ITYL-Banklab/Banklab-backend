package com.banklab.account.mapper;

import com.banklab.account.domain.AccountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Account Mapper 인터페이스
 * 계좌 테이블 삽입, 수정, 삭제 진행
 */
@Mapper
public interface AccountMapper {

    /**
     * Insert :실제 불러온 계좌 정보를 DB에 저장
     *
     * @param accountVO : API 호출로 불러온 계좌 정보
     * @return 보유 계좌 수
     */
    int insertAccount(AccountVO accountVO);

    /**
     * Select : memberId에 해당하는 계좌를 조회
     *
     * @param memberId 서비스 유저 아이디
     * @return 계좌 리스트를 반환
     */
    List<AccountVO> selectAccountsByUserId(@Param("memberId") Long memberId);

    /**
     * Update : 새로고침 후 잔액 업데이트
     *
     * @param memberId : 서비스 유저 아이디
     * @param resAccount : 계좌번호
     * @param newBalance : 업데이트 할 잔액
     */
    void updateAccountBalance(@Param("memberId") Long memberId, @Param("resAccount") String resAccount, @Param("newBalance") String newBalance);

    /**
     * Delete : 계좌 삭제
     *
     * @param memberId : 서비스 유저 아이디
     * @param connectedId : 커넥티드 아이디
     */
    void deleteAccount(@Param("memberId") Long memberId, @Param("connectedId") String connectedId);

}
