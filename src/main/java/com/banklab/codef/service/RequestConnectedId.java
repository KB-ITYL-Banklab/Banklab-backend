package com.banklab.codef.service;

import com.banklab.codef.util.ApiRequest;
import com.banklab.codef.util.CommonConstant;
import com.banklab.codef.util.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * 커넥티드 아이디 발급 요청하는 클래스
 *
 */
public class RequestConnectedId {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Create connected id.
     *
     * @param id : 은행 id
     * @param password : 은행 password
     * @Param bodyMap :  실제 json으로 변환할 해시테이블 <"accountList" : [accountMap]>
     * @Param accountMap : 데이터를 담은 해시테이블 <"countryCode" : "KR" ...>
     * @throws Exception the exception
     */
    public static String createConnectedId(String id, String password) throws Exception {
        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.CREATE_ACCOUNT;

        HashMap<String, Object> bodyMap = new HashMap<String, Object>();
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> accountMap = new HashMap<String, Object>();
        accountMap.put("countryCode",	"KR");
        accountMap.put("businessType",	"BK");
        accountMap.put("clientType",  	"P");
        accountMap.put("organization",	"0004");
        accountMap.put("loginType",  	"1");
        accountMap.put("id",id);
        accountMap.put("password",	RSAUtil.encryptRSA(password, CommonConstant.PUBLIC_KEY));
        list.add(accountMap);

        bodyMap.put("accountList", list);

        String result = ApiRequest.request(urlPath, bodyMap);
        //System.out.println(result);

        JsonNode root = mapper.readTree(result);
        JsonNode connectedIdNode = root.path("data").path("connectedId");

        if (connectedIdNode != null && !connectedIdNode.isNull()) {
            return connectedIdNode.asText();  // connectedId 반환
        }
        else {
            throw new RuntimeException("connectedId를 응답에서 찾을 수 없습니다.");
        }
    }
}
