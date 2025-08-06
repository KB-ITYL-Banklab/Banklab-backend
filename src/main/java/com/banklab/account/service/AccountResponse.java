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
        JsonNode resDepositTrustNode = root.path("data").path("resDepositTrust");

        if (resDepositTrustNode.isMissingNode() || resDepositTrustNode.isNull()) {
            log.error("resDepositTrust 데이터를 찾을 수 없습니다.");
            throw new RuntimeException("resDepositTrust 데이터를 찾을 수 없습니다.");
        }

        List<AccountVO> accountVOList = new ArrayList<>();

        for (JsonNode node : resDepositTrustNode) {
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

        log.info("계좌 정보 조회 완료 - 총 {}개 계좌", accountVOList.size());
        return accountVOList;
    }
}
