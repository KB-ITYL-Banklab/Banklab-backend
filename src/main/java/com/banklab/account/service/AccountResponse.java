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

    public static List<AccountVO> requestAccounts(String userId, String bankCode, String connectedId) throws Exception {
        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.KR_BK_1_P_001;

        HashMap<String, Object> bodyMap = new HashMap<String, Object>();
        bodyMap.put("organization", bankCode);
        bodyMap.put("connectedId", connectedId);
        bodyMap.put("birthDate", "");
        bodyMap.put("withdrawAccountNo", "");
        bodyMap.put("withdrawAccountPassword", "");


        String result = ApiRequest.request(urlPath, bodyMap);

        // Json Parsing
        JsonNode root = mapper.readTree(result);
        //System.out.println(root);
        JsonNode resDepositTrustNode = root.path("data").path("resDepositTrust");

        if (resDepositTrustNode.isMissingNode() || resDepositTrustNode.isNull()) {
            throw new RuntimeException("resDepositTrust 데이터를 찾을 수 없습니다.");
        }

        List<AccountVO> accountVOList = new ArrayList<>();

        for (JsonNode node : resDepositTrustNode) {
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setResAccount(node.get("resAccount").asText());
            accountDTO.setResAccountName(node.get("resAccountName").asText());
            accountDTO.setResAccountDisplay(node.get("resAccountDisplay").asText());
            accountDTO.setResAccountBalance(node.get("resAccountBalance").asText());

            // 출력 (디버깅용)
            log.info("계좌명: {}", accountDTO.getResAccountName());
            log.info("계좌번호: {}", accountDTO.getResAccount());
            log.info("표시용 번호: {}", accountDTO.getResAccountDisplay());
            log.info("잔액: {}", accountDTO.getResAccountBalance());
            log.info("---");

            // DTO → VO 변환 (비즈니스 정보 추가)
            AccountVO vo = accountDTO.toVO(userId, connectedId, bankCode);
            accountVOList.add(vo);
        }

        return accountVOList;
    }
}
