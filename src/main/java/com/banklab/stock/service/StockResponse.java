package com.banklab.stock.service;

import com.banklab.codef.util.ApiRequest;
import com.banklab.codef.util.CommonConstant;
import com.banklab.stock.domain.StockVO;
import com.banklab.stock.dto.StockDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class StockResponse {

    private static final ObjectMapper mapper = new ObjectMapper();


    public static List<StockVO> requestStocks(Long memberId, String stockCode, String connectedId, String account) throws Exception {
        log.info("계좌 정보 조회 요청 시작 - memberId: {}, 은행코드: {}, conn     " +
                "ectedId: {}", memberId, stockCode, connectedId);

        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.KR_ST_1_P_001;

        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("organization", stockCode);
        bodyMap.put("connectedId", connectedId);
        bodyMap.put("account", account);
        bodyMap.put("accountPassword", "");
        bodyMap.put("id", "");
        bodyMap.put("add_password", "");

        String result = ApiRequest.request(urlPath, bodyMap);

        // Json Parsing
        JsonNode root = mapper.readTree(result);
        log.info("root 노드 확인 : {}", root);

        JsonNode dataNode = root.path("data");

        // 계좌번호, 예수금
        String resAccount = dataNode.path("resAccount").asText(); // 증권 계좌
        String resDepositReceived = dataNode.path("resDepositReceived").asText(); // 예수금

        log.info("계좌번호 : {}", resAccount);
        log.info("예수금 : {}", resDepositReceived);

        JsonNode resItemList = dataNode.path("resItemList");

        if(resItemList.isMissingNode() || resItemList.isNull()) {
            log.error("resItemList를 찾을 수 없습니다.");
            throw new RuntimeException("resItemList를 찾을 수 없습니다.");
        }

        List<StockVO> stockVOList = new ArrayList<>();

        for(JsonNode resItem : resItemList) {
            StockDTO stockDTO = new StockDTO();

            stockDTO.setResAccount(resAccount);
            stockDTO.setResDepositReceived(resDepositReceived);

            // 종목별 정보 (resItem에서 가져오기)
            stockDTO.setResItemName(resItem.path("resItemName").asText());
            stockDTO.setResItemCode(resItem.path("resItemCode").asText());
            stockDTO.setResQuantity(resItem.path("resQuantity").asText());
            stockDTO.setResValuationAmt(resItem.path("resValuationAmt").asText());
            stockDTO.setResPurchaseAmount(resItem.path("resPurchaseAmount").asText());
            stockDTO.setResValuationPL(resItem.path("resValuationPL").asText());
            stockDTO.setResEarningsRate(resItem.path("resEarningsRate").asText());

            // 출력
            log.info("종목명 : {}", stockDTO.getResItemName());
            log.info("종목코드 : {}", stockDTO.getResItemCode());
            log.info("수량 : {}", stockDTO.getResQuantity());
            log.info("평가금액 : {}", stockDTO.getResValuationAmt());
            log.info("매입금액 : {}", stockDTO.getResPurchaseAmount());
            log.info("평가손익 : {}", stockDTO.getResValuationPL());
            log.info("수익률 : {}", stockDTO.getResEarningsRate());

            StockVO vo = stockDTO.toVO(memberId, connectedId, stockCode);
            stockVOList.add(vo);
        }

        log.info("보유 종목 수 : {}", stockVOList.size());
        return stockVOList;
    }
}
