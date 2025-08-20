package com.banklab.transaction.service;

import com.banklab.codef.util.ApiRequest;
import com.banklab.codef.util.CommonConstant;
import com.banklab.transaction.domain.TransactionHistoryVO;
import com.banklab.transaction.dto.request.TransactionDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Log4j2
@Service // Add @Service annotation
@RequiredArgsConstructor
public class TransactionResponse {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * CODEF API를 호출하여 거래 내역을 조회하고, 결과를 TransactionHistoryVO 리스트로 파싱합니다.
     * @param memberId 사용자 ID
     * @param request 거래 내역 조회를 위한 필수 파라미터 (connectedId, organization, account 등)
     * @return 파싱된 거래 내역 VO 리스트
     * @throws IOException API 요청 또는 JSON 파싱 중 오류 발생 시
     * @throws InterruptedException API 요청 중 인터럽트 발생 시
     */
    public static List<TransactionHistoryVO> requestTransactions(Long memberId, TransactionDTO request) throws IOException, InterruptedException {
        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.KR_BK_1_P_002;

        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("connectedId", request.getConnectedId());
        bodyMap.put("organization", request.getOrganization());
        bodyMap.put("account", request.getAccount());
        bodyMap.put("startDate", request.getStartDate());
        bodyMap.put("endDate", request.getEndDate());
        bodyMap.put("orderBy", request.getOrderBy());


        String result = ApiRequest.request(urlPath, bodyMap); // Use injected apiRequest

        //Json parsing
        JsonNode root = mapper.readTree(result);
        JsonNode dataNode = root.path("data");
        JsonNode list = dataNode.path("resTrHistoryList");;

        if (dataNode.isMissingNode() || dataNode.isNull()) {
            throw new RuntimeException("데이터를 찾을 수 없습니다.");
        }

        List<TransactionHistoryVO> transactions = new ArrayList<>();

        for (JsonNode item : list) {
            TransactionHistoryVO vo = mapper.treeToValue(item, TransactionHistoryVO.class);
            vo.setResAccount(dataNode.path("resAccount").asText());
            vo.setMemberId(memberId);
            vo.setCategory_id(8L);
            transactions.add(vo) ;
        }

        return transactions;
    }





}