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
        log.info("계좌 정보 조회 요청 시작 - memberId: {}, 은행코드: {}, connectedId: {}", memberId, bankCode, connectedId);

        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.KR_BK_1_P_001;

        HashMap<String, Object> bodyMap = new HashMap<String, Object>();
        bodyMap.put("organization", bankCode);
        bodyMap.put("connectedId", connectedId);
        bodyMap.put("birthDate", "");
        bodyMap.put("withdrawAccountNo", "");
        bodyMap.put("withdrawAccountPassword", "");


        String result = ApiRequest.request(urlPath, bodyMap);
        log.info("🔍 CODEF API 전체 응답: " + result);

        // Json Parsing
        JsonNode root = mapper.readTree(result);
        //System.out.println(root);
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

        log.info("계좌 정보 조회 완료 - 총 {}개 계좌", accountVOList.size());
        return accountVOList;
    }

    private static void processAccount(JsonNode dataNode, String accountType, String typeName,
                                   List<AccountVO> accountVOList, Long memberId, String connectedId, String bankCode) {
        log.info(" {} 계좌 처리 중", typeName);
        
        JsonNode accountTypeNode = dataNode.path(accountType);

        // 데이터 필드가 없는 경우 예외
        if (accountTypeNode.isMissingNode() || accountTypeNode.isNull() || !accountTypeNode.isArray()) {
            log.info("{} 데이터 없음 (정상)", typeName);
            return;
        }

        // 해당 유형의 계좌가 없는 경우
        if (accountTypeNode.size() == 0) {
            log.info("{} 계좌 없음 (정상)", typeName);
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

            // 출력 (디버깅용)
            log.info("계좌명: {}", accountDTO.getResAccountName());
            log.info("계좌번호: {}", accountDTO.getResAccount());
            log.info("표시용 번호: {}", accountDTO.getResAccountDisplay());
            log.info("잔액: {}", accountDTO.getResAccountBalance());
            log.info("예금구분: {}", accountDTO.getResAccountDeposit());
            log.info("가입일: {}", accountDTO.getResAccountStartDate());
            log.info("만기일: {}", accountDTO.getResAccountEndDate());
            log.info("---");

            // DTO → VO 변환 (비즈니스 정보 추가)
            AccountVO vo = accountDTO.toVO(memberId, connectedId, bankCode);
            accountVOList.add(vo);
        }
        
        log.info("{} 처리 완료 - {}개 계좌 추가됨", typeName, accountTypeNode.size());
    }
}