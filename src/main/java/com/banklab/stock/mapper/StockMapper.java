package com.banklab.stock.mapper;

import com.banklab.stock.domain.StockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Stock Mapper 인터페이스
 * 증권계좌 및 보유종목 테이블 삽입, 수정, 삭제 진행
 */
@Mapper
public interface StockMapper {

    /**
     * Insert : 여러 보유종목을 한 번에 삽입 (CODEF API 응답 처리용)
     *
     * @param stockList : 보유종목 리스트 (한 계좌의 여러 종목)
     */
    void insertStockList(@Param("stockList") List<StockVO> stockList);

    /**
     * Select : 사용자의 모든 보유종목 조회
     *
     * @param memberId 서비스 유저 아이디
     * @return 보유 종목 리스트 (프론트에서 증권사/계좌별 필터링)
     */
    List<StockVO> getStocksByMemberId(@Param("memberId") Long memberId);

    /**
     * Delete : 특정 connectedId의 모든 보유종목 삭제 (계좌 연결 해제시)
     * 커넥티드 아이디 = 은행 별 연결 증권
     *
     * @param memberId 서비스 유저 아이디
     * @param connectedId 커넥티드 아이디
     */
    void deleteStocksByConnectedId(@Param("memberId") Long memberId,
                                   @Param("connectedId") String connectedId);

}