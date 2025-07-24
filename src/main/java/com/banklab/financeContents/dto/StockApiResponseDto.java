package com.banklab.financeContents.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 공공데이터포털 주식 API 응답 전체 구조 DTO
 * 
 * 이 클래스는 공공데이터포털의 주식시세정보 API로부터 받은 JSON 응답을 
 * 자바 객체로 매핑하기 위한 Data Transfer Object입니다.
 * 
 * API 응답 구조:
 * {
 *   "response": {
 *     "header": {
 *       "resultCode": "00",     // 결과 코드 (00: 성공)
 *       "resultMsg": "NORMAL_SERVICE"  // 결과 메시지
 *     },
 *     "body": {
 *       "numOfRows": 10,        // 한 페이지 결과 수
 *       "pageNo": 1,            // 페이지 번호
 *       "totalCount": 2500,     // 전체 데이터 수
 *       "items": {
 *         "item": [...]         // 실제 주식 데이터 배열
 *       }
 *     }
 *   }
 * }
 * 
 * 중첩된 정적 클래스들을 사용하여 JSON의 계층 구조를 그대로 반영했습니다.
 */
@Data
public class StockApiResponseDto {
    
    @JsonProperty("response")
    private ResponseBody response;
    
    /**
     * API 응답의 본문 부분을 나타내는 클래스
     * header(결과 정보)와 body(실제 데이터)를 포함합니다.
     */
    @Data
    public static class ResponseBody {
        
        @JsonProperty("header")
        private Header header;
        
        @JsonProperty("body")
        private Body body;
        
        /**
         * API 호출 결과 정보를 담는 헤더 클래스
         * 성공/실패 여부와 결과 메시지를 포함합니다.
         */
        @Data
        public static class Header {
            /** API 호출 결과 코드 ("00": 정상, 그 외: 오류) */
            @JsonProperty("resultCode")
            private String resultCode;
            
            /** API 호출 결과 메시지 ("NORMAL_SERVICE": 정상) */
            @JsonProperty("resultMsg")
            private String resultMsg;
        }
        
        /**
         * API 응답의 실제 데이터 부분을 나타내는 클래스
         * 페이징 정보와 주식 데이터 목록을 포함합니다.
         */
        @Data
        public static class Body {
            /** 한 페이지당 조회된 데이터 수 */
            @JsonProperty("numOfRows")
            private int numOfRows;
            
            /** 현재 페이지 번호 */
            @JsonProperty("pageNo")
            private int pageNo;
            
            /** 전체 데이터 개수 */
            @JsonProperty("totalCount")
            private int totalCount;
            
            /** 실제 주식 데이터 배열을 포함하는 객체 */
            @JsonProperty("items")
            private Items items;
            
            /**
             * 주식 데이터 배열을 감싸는 컨테이너 클래스
             * 공공데이터포털 API의 표준 응답 형식을 따릅니다.
             */
            @Data
            public static class Items {
                /** 주식 정보 객체들의 리스트 */
                @JsonProperty("item")
                private List<StockSecurityInfoDto> item;
            }
        }
    }
}
