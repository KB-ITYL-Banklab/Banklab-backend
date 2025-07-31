package com.banklab.codef.service;

import com.banklab.codef.util.ApiRequest;
import com.banklab.codef.util.CommonConstant;
import com.banklab.codef.util.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.util.*;

/**
 * 커넥티드 아이디 발급 및 삭제 요청하는 클래스
 * @Method createConnectedId : 발급
 * @Method deleteConnectedId : 삭제
 *
 */
@Log4j2
public class RequestConnectedId {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 커넥티드 아이디 발급
     *
     * @param id : 은행/증권 id
     * @param password : 은행/증권 password
     * @param organization : 기관코드
     * @param businessType : BK : 은행, ST : 주식, CD : 카드
     * @param clientType : 개인 : P, 법인 : B, 통합 : A (증권은 통합임)
     * @implNote bodyMap :  실제 json으로 변환할 해시테이블 <"accountList" : [accountMap]>
     * @implNote accountMap: 데이터를 담은 해시테이블 <"countryCode" : "KR" ...>
     * @throws Exception the exception
     */
    public static String createConnectedId(String id, String password, String organization, String businessType, String clientType) throws Exception {
        log.info("🏦 커넥티드 아이디 발급 요청 시작 - 은행코드: {}, ID: {}", organization, id);

        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.CREATE_ACCOUNT;

        HashMap<String, Object> bodyMap = new HashMap<String, Object>();
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> accountMap = new HashMap<String, Object>();
        accountMap.put("countryCode", "KR");
        accountMap.put("businessType", businessType);
        accountMap.put("clientType", clientType);
        accountMap.put("organization", organization);
        accountMap.put("loginType", "1");
        accountMap.put("id", id);
        accountMap.put("password", RSAUtil.encryptRSA(password, CommonConstant.PUBLIC_KEY));
        list.add(accountMap);

        bodyMap.put("accountList", list);

        String result = ApiRequest.request(urlPath, bodyMap);
        //System.out.println(result);

        JsonNode root = mapper.readTree(result);

        JsonNode connectedIdNode = root.path("data").path("connectedId");
        if (connectedIdNode != null && !connectedIdNode.isNull()) {
            String connectedId = connectedIdNode.asText();
            log.info("커넥티드 아이디 발급 완료: {}", connectedId);
            return connectedId;  // connectedId 반환
        }
        else {
            log.error("connectedId를 응답에서 찾을 수 없습니다.");
            throw new RuntimeException("connectedId를 응답에서 찾을 수 없습니다.");
        }
    }

    /**
     * 커넥티드 아이디 삭제
     *
     * @param connectedId : 삭제하고자 하는 커넥티드 아이디
     * @param organization : 기관코드
     * @return T/F
     * @throws Exception the exception
     */
    public static boolean deleteConnectedId(String connectedId, String organization, String businessType) throws Exception {
        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.DELETE_ACCOUNT;

        HashMap<String, Object> bodyMap = new HashMap<String, Object>();
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> accountMap = new HashMap<String, Object>();
        accountMap.put("countryCode",	"KR");
        accountMap.put("businessType",	businessType);
        accountMap.put("clientType",  	"A");
        accountMap.put("organization",	organization);
        accountMap.put("loginType",  	"1");
        list.add(accountMap);

        bodyMap.put("accountList", list);
        bodyMap.put("connectedId", connectedId);

        String result = ApiRequest.request(urlPath, bodyMap);

        JsonNode root = mapper.readTree(result);

        JsonNode resultNode = root.path("result");
        if (resultNode != null && !resultNode.isNull()) {
            String resultCode = resultNode.path("code").asText();
            log.info("응답 코드 : {}", resultCode);

            boolean isSuccess = "CF-00000".equals(resultCode);
            if (isSuccess) {
                log.info("connectedId '{}'의 삭제가 성공적으로 진행되었습니다!", connectedId);
            } else {
                log.warn("connectedId '{}'의 삭제가 실패했습니다. 응답 코드: {}", connectedId, resultCode);
            }
            return isSuccess;
        }

        return false;
    }

}

