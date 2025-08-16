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


    public static List<StockVO> requestStocks(Long memberId, String stockCode, String connectedId, String account, String accountPassword) throws Exception {
        log.info("계좌 정보 조회 요청 시작 - memberId: {}, 은행코드: {}, connectedId: {}", memberId, stockCode, connectedId);

        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.KR_ST_1_P_005;

        if(accountPassword == null || accountPassword.isEmpty()) accountPassword = "";

        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("organization", stockCode);
        bodyMap.put("connectedId", connectedId);
        bodyMap.put("account", account);
        bodyMap.put("accountPassword", accountPassword);
        bodyMap.put("id", "");
        bodyMap.put("add_password", "");

        String result = ApiRequest.request(urlPath, bodyMap);
        log.info("CODEF API 응답 : {}", result);

        // Json Parsing
        JsonNode root = mapper.readTree(result);

        // 1. 먼저 결과 코드 확인
        JsonNode resultNode = root.path("result");
        if (resultNode.isMissingNode() || resultNode.isNull()) {
            log.error("응답에서 result 필드를 찾을 수 없습니다.");
            throw new RuntimeException("CODEF API 응답 형식이 올바르지 않습니다.");
        }

        String resultCode = resultNode.path("code").asText();
        String resultMessage = resultNode.path("message").asText();

        // 2. 성공 코드가 아닌 경우 예외 처리
        if (!"CF-00000".equals(resultCode)) {
            log.error("증권 보유종목 조회 실패 - 코드: {}, 메시지: {}", resultCode, resultMessage);

            // data.errorList에서 구체적인 에러 정보 확인 (우선순위)
            String finalErrorCode = resultCode;
            String finalErrorMessage = resultMessage;

            JsonNode dataNode = root.path("data");
            if (!dataNode.isMissingNode() && !dataNode.isNull()) {
                JsonNode errorListNode = dataNode.path("errorList");
                if (errorListNode.isArray() && errorListNode.size() > 0) {
                    JsonNode firstError = errorListNode.get(0);
                    String errorCode = firstError.path("code").asText();
                    String errorMessage = firstError.path("message").asText();

                    if (!errorCode.isEmpty()) {
                        // errorList의 에러 정보를 우선 사용
                        finalErrorCode = errorCode;
                        finalErrorMessage = errorMessage;
                        log.info("🔍 data.errorList에서 구체적인 에러 정보 사용 - 코드: {}, 메시지: {}", finalErrorCode, finalErrorMessage);
                    }
                }
            }

            // 특정 에러 코드에 따른 사용자 친화적 메시지
            String userMessage = getUserFriendlyErrorMessage(finalErrorCode, finalErrorMessage);
            throw new RuntimeException(userMessage);
        }

        // 3. 성공한 경우에만 데이터 추출
        JsonNode dataNode = root.path("data");

        // 계좌번호, 예수금
        String resAccount = dataNode.path("resAccount").asText(); // 증권 계좌
        String resDepositReceived = dataNode.path("resDepositReceived").asText(); // 예수금
        String resDepositReceivedD1 = dataNode.path("resDepositReceivedD1").asText(); // 예수금 D+1
        String resDepositReceivedD2 = dataNode.path("resDepositReceivedD2").asText(); // 예수금 D+2

        log.info("계좌번호 : {}", resAccount);
        log.info("예수금 : {}", resDepositReceived);
        log.info("예수금 D+1 : {}", resDepositReceivedD1);
        log.info("예수금 D+2 : {}", resDepositReceivedD2);

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
            stockDTO.setResProductType(resItem.path("resProductType").asText());
            stockDTO.setResItemName(resItem.path("resItemName").asText());
            stockDTO.setResItemCode(resItem.path("resItemCode").asText());
            stockDTO.setResQuantity(resItem.path("resQuantity").asText());
            stockDTO.setResPresentAmt(resItem.path("resPresentAmt").asText());
            stockDTO.setResPurchaseAmount(resItem.path("resPurchaseAmount").asText());
            stockDTO.setResValuationAmt(resItem.path("resValuationAmt").asText());
            stockDTO.setResValuationPL(resItem.path("resValuationPL").asText());
            stockDTO.setResEarningsRate(resItem.path("resEarningsRate").asText());
            stockDTO.setResAccountCurrency(resItem.path("resAccountCurrency").asText());

            // 출력
            log.info("상품유형 : {}", stockDTO.getResProductType());
            log.info("종목명 : {}", stockDTO.getResItemName());
            log.info("종목코드 : {}", stockDTO.getResItemCode());
            log.info("수량 : {}", stockDTO.getResQuantity());
            log.info("현재가 : {}원", stockDTO.getResPresentAmt());
            log.info("매입금액 : {}원", stockDTO.getResPurchaseAmount());
            log.info("평가금액 : {}원", stockDTO.getResValuationAmt());
            log.info("평가손익 : {}원", stockDTO.getResValuationPL());
            log.info("수익률 : {}%", stockDTO.getResEarningsRate());
            log.info("통화코드 : {}", stockDTO.getResAccountCurrency());

            StockVO vo = stockDTO.toVO(memberId, connectedId, stockCode);
            stockVOList.add(vo);
        }

        log.info("보유 종목 수 : {}", stockVOList.size());
        return stockVOList;
    }

    /**
     * 에러 코드에 따른 사용자 친화적 메시지 반환
     */
    private static String getUserFriendlyErrorMessage(String resultCode, String resultMessage) {
        switch (resultCode) {
            case "CF-12803":
                return "아이디 또는 비밀번호가 올바르지 않습니다.\n로그인 정보를 확인 후 다시 시도해주세요.";
            case "CF-12703":
                return "증권사 서버에 일시적인 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.";
            case "CF-04000":
                return "증권계좌 연동에 실패했습니다.\n로그인 정보를 확인해주세요.";
            default:
                // 알려지지 않은 에러코드는 원본 메시지를 개행 처리하여 전달
                return String.format("증권 보유종목 조회에 실패했습니다.\n(오류코드: %s)\n%s", resultCode, resultMessage);
        }
    }
}
