package com.banklab.stock.service;

import com.banklab.stock.domain.StockVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 증권 서비스 인터페이스
 */
public interface StockService {

    /**
     * 증권을 DB에 저장
     *
     * @param stockVOList 보유종목 리스트
     */
     void saveStocks(List<StockVO> stockVOList);

    /**
     * DB에 저장된 보유종목을 조회
     *
     * @param memberId 사용자 ID
     * @return 보유종목 리스트
     */
    List<StockVO> getUserStocks(Long memberId);

    /**
     * API 재호출해서 보유종목 정보를 새로고침
     *
     * @param memberId 사용자 ID
     * @param stockCode 증권사 코드
     * @param connectedId 커넥티드 ID
     * @param account 계좌번호
     */
    void refreshUserStocks(Long memberId, String stockCode, String connectedId, String account) throws Exception;

    /**
     * DB에 저장된 보유종목을 삭제 (계좌 연결 해제)
     *
     * @param memberId 사용자 ID
     * @param connectedId 커넥티드 ID
     * memberId와 connectedId 두 개 모두 일치하는 파라미터가 들어왔을 때 삭제
     */
    void disconnectUserStocks(Long memberId, String connectedId);

}
