package com.banklab.financeContents.service;

import com.banklab.financeContents.dto.FinanceTermsDto;
import com.banklab.financeContents.dto.FinanceTermsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * 금융용어 서비스
 * SEIBRO API를 사용하여 금융용어 정보를 조회하는 서비스
 */
@Slf4j
@Service
public class FinanceTermsService {
    
    @Value("${seibro.api.url}")
    private String API_URL;
    
    @Value("${seibro.api.key}")
    private String AUTH_KEY;
    
    private final HttpClient httpClient;
    
    /**
     * 생성자 - HttpClient 초기화
     */
    public FinanceTermsService() {
        this.httpClient = HttpClientBuilder.create().build();
    }
    
    /**
     * 금융용어 검색
     * 
     * @param term 검색할 금융용어
     * @return 금융용어 응답 정보
     */
    public FinanceTermsResponse getFinanceTerm(String term) {
        try {
            log.info("금융용어 검색 시작: {}", term);
            
            // 입력값 검증
            if (term == null || term.trim().isEmpty()) {
                log.warn("검색어가 비어있습니다.");
                return FinanceTermsResponse.failure(term, "검색어를 입력해주세요.");
            }
            
            String url = buildApiUrl(term.trim());
            log.info("API 요청 URL: {}", url);
            
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.debug("API 응답 길이: {}", responseBody != null ? responseBody.length() : 0);
            
            if (response.getStatusLine().getStatusCode() == 200) {
                if (responseBody == null || responseBody.trim().isEmpty()) {
                    log.warn("API 응답이 비어있습니다.");
                    return FinanceTermsResponse.failure(term, "API 응답이 비어있습니다.");
                }
                
                // XML 응답 파싱
                FinanceTermsDto termDto = parseXmlResponse(responseBody, term);
                
                if (termDto != null) {
                    log.info("금융용어 검색 성공: {}", termDto);
                    return FinanceTermsResponse.success(term, termDto);
                } else {
                    log.warn("검색 결과가 없습니다: {}", term);
                    return FinanceTermsResponse.failure(term, "해당 금융용어에 대한 정보를 찾을 수 없습니다.");
                }
                
            } else {
                log.error("API 호출 실패. 상태코드: {}", response.getStatusLine().getStatusCode());
                return FinanceTermsResponse.failure(term, "API 호출 실패: " + response.getStatusLine().getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("금융용어 검색 중 오류 발생: {}", e.getMessage(), e);
            return FinanceTermsResponse.failure(term, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * XML 응답을 FinanceTermsDto로 파싱
     * 
     * @param xmlResponse XML 형태의 API 응답
     * @param searchTerm 검색한 용어
     * @return 파싱된 금융용어 정보
     */
    private FinanceTermsDto parseXmlResponse(String xmlResponse, String searchTerm) {
        try {
            log.debug("XML 파싱 시작");
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));
            
            // resultCode 확인
            NodeList resultCodeNodes = document.getElementsByTagName("resultCode");
            if (resultCodeNodes.getLength() > 0) {
                String resultCode = resultCodeNodes.item(0).getTextContent();
                log.debug("결과 코드: {}", resultCode);
                
                if (!"00".equals(resultCode)) {
                    NodeList resultMsgNodes = document.getElementsByTagName("resultMsg");
                    String resultMsg = resultMsgNodes.getLength() > 0 ? 
                        resultMsgNodes.item(0).getTextContent() : "알 수 없는 오류";
                    log.warn("API 오류 - 코드: {}, 메시지: {}", resultCode, resultMsg);
                    return null;
                }
            }
            
            // item 데이터 추출
            NodeList itemNodes = document.getElementsByTagName("item");
            if (itemNodes.getLength() > 0) {
                Element item = (Element) itemNodes.item(0);
                
                String id = getTextContent(item, "fnceDictNm");
                String title = getTextContent(item, "term");
                String definition = getTextContent(item, "ksdFnceDictDescContent");
                
                // title이 비어있으면 검색어 사용
                if (title == null || title.trim().isEmpty()) {
                    title = searchTerm;
                }
                
                log.debug("파싱 결과 - ID: {}, 제목: {}, 정의 길이: {}", 
                    id, title, definition != null ? definition.length() : 0);
                
                return FinanceTermsDto.builder()
                    .id(parseId(id))
                    .title(title)
                    .definition(definition != null ? definition : "정의 정보가 없습니다.")
                    .build();
            } else {
                log.warn("검색 결과가 없습니다: {}", searchTerm);
                return null;
            }
            
        } catch (Exception e) {
            log.error("XML 파싱 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Element에서 지정된 태그의 텍스트 내용 추출
     */
    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
    
    /**
     * 문자열을 Integer로 파싱 (실패 시 null 반환)
     */
    private Integer parseId(String idStr) {
        try {
            return idStr != null && !idStr.trim().isEmpty() ? Integer.parseInt(idStr.trim()) : null;
        } catch (NumberFormatException e) {
            log.debug("ID 파싱 실패: {}", idStr);
            return null;
        }
    }
    
    /**
     * API URL 생성
     * @param term 검색할 금융용어
     * @return API URL
     */
    private String buildApiUrl(String term) {
        return String.format("%s?serviceKey=%s&term=%s&numOfRows=1&pageNo=1", 
            API_URL, AUTH_KEY, term);
    }
}
