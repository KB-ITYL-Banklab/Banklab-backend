package com.banklab.account.service;

import com.banklab.account.domain.AccountVO;
import com.banklab.account.dto.AccountDTO;
import com.banklab.codef.util.ApiRequest;
import com.banklab.codef.util.CommonConstant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Log4j2
public class AccountResponse {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<AccountVO> requestAccounts(Long memberId, String bankCode, String connectedId) throws Exception {

        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.KR_BK_1_P_001;

        HashMap<String, Object> bodyMap = new HashMap<String, Object>();
        bodyMap.put("organization", bankCode);
        bodyMap.put("connectedId", connectedId);
        bodyMap.put("birthDate", "");
        bodyMap.put("withdrawAccountNo", "");
        bodyMap.put("withdrawAccountPassword", "");


        String result = ApiRequest.request(urlPath, bodyMap);
        //log.info("CODEF API 전체 응답: " + result);

        // Json Parsing
        JsonNode root = mapper.readTree(result);

        JsonNode resultNode = root.path("result");
        if (resultNode.isMissingNode() || resultNode.isNull()) {
            log.error("응답에서 result 필드를 찾을 수 없습니다.");
            throw new RuntimeException("CODEF API 응답 형식이 올바르지 않습니다.");
        }

        String resultCode = resultNode.path("code").asText();
        String resultMessage = resultNode.path("message").asText();

        // 2. 성공 코드가 아닌 경우 예외 처리
        if (!"CF-00000".equals(resultCode)) {
            log.error("계좌 정보 조회 실패 - 코드: {}, 메시지: {}", resultCode, resultMessage);

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
                    }
                }
            }

            // 특정 에러 코드에 따른 사용자 친화적 메시지
            String userMessage = getUserFriendlyErrorMessage(finalErrorCode, finalErrorMessage);
            throw new RuntimeException(userMessage);
        }

        // 3. 성공한 경우에만 데이터 추출
        JsonNode dataNode = root.path("data");

        if (dataNode.isMissingNode() || dataNode.isNull()) {
            log.error("데이터를 찾을 수 없습니다.");
            throw new RuntimeException("응답 데이터를 찾을 수 없습니다.");
        }

        List<AccountVO> accountVOList = new ArrayList<>();

        // 계좌 유형별 매핑 (API 필드명 → 한글명)
        HashMap<String, String> typeMap = new HashMap<>();
        typeMap.put("resDepositTrust", "예금신탁");
        typeMap.put("resForeignCurrency", "외화계좌");
        typeMap.put("resFund", "펀드");
        typeMap.put("resLoan", "대출");
        typeMap.put("resInsurance", "보험");

        // HashMap을 순회하며 모든 계좌 유형 처리
        for (String accountType : typeMap.keySet()) {
            String typeName = typeMap.get(accountType);
            processAccount(dataNode, accountType, typeName, accountVOList, memberId, connectedId, bankCode);
        }

        return accountVOList;
    }

    /**
     * 에러 코드에 따른 사용자 친화적 메시지 반환
     */
    private static String getUserFriendlyErrorMessage(String resultCode, String resultMessage) {
        switch (resultCode) {
            case "CF-12803":
                return "아이디 또는 비밀번호가 올바르지 않습니다.\n로그인 정보를 확인 후 다시 시도해주세요.";
            case "CF-12703":
                return "은행 서버에 일시적인 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.";
            case "CF-04000":
                return "계좌 정보 조회에 실패했습니다.\n로그인 정보를 확인해주세요.";
            default:
                // 알려지지 않은 에러코드는 원본 메시지를 개행 처리하여 전달
                return String.format("계좌 정보 조회에 실패했습니다.\n(오류코드: %s)\n%s", resultCode, resultMessage);
        }
    }


    private static void processAccount(JsonNode dataNode, String accountType, String typeName,
                                   List<AccountVO> accountVOList, Long memberId, String connectedId, String bankCode) {

        JsonNode accountTypeNode = dataNode.path(accountType);

        // 데이터 필드가 없는 경우 예외
        if (accountTypeNode.isMissingNode() || accountTypeNode.isNull() || !accountTypeNode.isArray()) {
            return;
        }

        // 해당 유형의 계좌가 없는 경우
        if (accountTypeNode.size() == 0) {
            return;
        }

        for (JsonNode node : accountTypeNode) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setResAccount(node.get("resAccount").asText());
            accountDTO.setResAccountName(node.get("resAccountName").asText());
            accountDTO.setResAccountDisplay(node.get("resAccountDisplay").asText());
            accountDTO.setResAccountBalance(node.get("resAccountBalance").asText());
            accountDTO.setResAccountDeposit(node.get("resAccountDeposit").asText());
            accountDTO.setResAccountEndDate(node.get("resAccountEndDate").asText());
            accountDTO.setResAccountStartDate(node.get("resAccountStartDate").asText());


            // DTO → VO 변환 (비즈니스 정보 추가)
            AccountVO vo = accountDTO.toVO(memberId, connectedId, bankCode);
            accountVOList.add(vo);
        }

    }
}