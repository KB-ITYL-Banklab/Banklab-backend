package com.banklab.stock.service;

import com.banklab.stock.domain.StockVO;
import com.banklab.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;

    @Override
    @Transactional
    public void saveStocks(List<StockVO> stockVOList) {

        stockMapper.insertStockList(stockVOList);

        log.info("{}개 보유종목 저장 완료", stockVOList.size());
    }

    @Override
    public List<StockVO> getUserStocks(Long memberId) {
        return stockMapper.getStocksByMemberId(memberId);
    }

    @Override
    @Transactional
    public void refreshUserStocks(Long memberId, String stockCode, String connectedId, String account) throws Exception {

        // 1. 기존 데이터 삭제
        stockMapper.deleteStocksByConnectedId(memberId, connectedId);
        log.info("기존 보유종목 삭제 완료 - connectedId: {}", connectedId);

        // 2. 새 데이터 조회 & 저장
        List<StockVO> stockList = StockResponse.requestStocks(memberId, stockCode, connectedId, account, "");

        if (!stockList.isEmpty()) {
            stockMapper.insertStockList(stockList);
            log.info("증권계좌 정보 갱신 완료 - 보유종목 수: {}", stockList.size());
        } else {
            log.warn("갱신할 보유종목이 없습니다 - memberId: {}", memberId);
        }

    }

    @Override
    @Transactional
    public void disconnectUserStocks(Long memberId, String connectedId) {
        stockMapper.deleteStocksByConnectedId(memberId, connectedId);
    }

}
