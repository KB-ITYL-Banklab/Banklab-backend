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
     * @param id : 은행 id
     * @param password : 은행 password
     * @Variable bodyMap :  실제 json으로 변환할 해시테이블 <"accountList" : [accountMap]>
     * @Variable accountMap: 데이터를 담은 해시테이블 <"countryCode" : "KR" ...>
     * @throws Exception the exception
     */
    public static String createConnectedId(String id, String password, String organization, String businessType, String clientType) throws Exception {
        log.info("커넥티드 아이디 발급 요청 시작 - 은행코드: {}, ID: {}", organization, id);

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
        log.info("🔍 CODEF API 전체 응답: " + result);

        JsonNode root = mapper.readTree(result);

        // 1. 먼저 결과 코드 확인
        JsonNode resultNode = root.path("result");
        if (resultNode.isMissingNode() || resultNode.isNull()) {
            log.error("응답에서 result 필드를 찾을 수 없습니다.");
            throw new RuntimeException("CODEF API 응답 형식이 올바르지 않습니다.");
        }

        String resultCode = resultNode.path("code").asText();
        String resultMessage = resultNode.path("message").asText();

        log.info("🔍 CODEF API 결과 코드: {}, 메시지: {}", resultCode, resultMessage);

        // 2. 성공 코드가 아닌 경우 예외 처리
        if (!"CF-00000".equals(resultCode)) {
            log.error("커넥티드 아이디 발급 실패 - 코드: {}, 메시지: {}", resultCode, resultMessage);

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

        // 3. 성공한 경우에만 connectedId 추출
        JsonNode connectedIdNode = root.path("data").path("connectedId");
        if (connectedIdNode.isMissingNode() || connectedIdNode.isNull() || connectedIdNode.asText().isEmpty()) {
            log.error("성공 응답이지만 connectedId를 찾을 수 없습니다.");
            throw new RuntimeException("계좌 연결 정보를 생성할 수 없습니다.");
        }

        String connectedId = connectedIdNode.asText();
        log.info("🔍 추출된 connectedId: {}", connectedId);
        log.info("커넥티드 아이디 발급 완료: {}", connectedId);

        return connectedId;
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
                return "계좌 연결에 실패했습니다.\n로그인 정보를 확인해주세요.";
            default:
                // 알려지지 않은 에러코드는 원본 메시지를 개행 처리하여 전달
                return String.format("계좌 연결에 실패했습니다.\n(오류코드: %s)\n%s", resultCode, resultMessage);
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
    public static boolean deleteConnectedId(String connectedId, String organization, String businessType, String clientType) throws Exception {
        log.info("커넥티드 아이디 삭제 요청 시작 - connectedId: {}, 은행코드: {}", connectedId, organization);

        String urlPath = CommonConstant.TEST_DOMAIN + CommonConstant.DELETE_ACCOUNT;

        HashMap<String, Object> bodyMap = new HashMap<String, Object>();
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> accountMap = new HashMap<String, Object>();
        accountMap.put("countryCode",	"KR");
        accountMap.put("businessType",	businessType);
        accountMap.put("clientType",  	clientType);
        accountMap.put("organization",	organization);
        accountMap.put("loginType",  	"1");
        list.add(accountMap);

        bodyMap.put("accountList", list);
        bodyMap.put("connectedId", connectedId);

        String result = ApiRequest.request(urlPath, bodyMap);
        log.info("🔍 CODEF API 삭제 응답: " + result);

        JsonNode root = mapper.readTree(result);

        JsonNode resultNode = root.path("result");
        if (resultNode != null && !resultNode.isNull()) {
            String resultCode = resultNode.path("code").asText();
            String resultMessage = resultNode.path("message").asText();
            log.info("삭제 응답 코드: {}, 메시지: {}", resultCode, resultMessage);

            boolean isSuccess = "CF-00000".equals(resultCode);
            if (isSuccess) {
                log.info("connectedId '{}'의 삭제가 성공적으로 진행되었습니다!", connectedId);
            } else {
                log.warn("connectedId '{}'의 삭제가 실패했습니다. 응답 코드: {}, 메시지: {}", connectedId, resultCode, resultMessage);
            }
            return isSuccess;
        }

        log.error("삭제 응답에서 result 필드를 찾을 수 없습니다.");
        return false;
    }
}