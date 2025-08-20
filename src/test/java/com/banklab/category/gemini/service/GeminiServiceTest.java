package com.banklab.category.gemini.service;

import com.banklab.category.gemini.dto.GeminiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * GeminiService에 대한 테스트 클래스
 */
@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GeminiService geminiService;

    private final String apiUrl = "https://test-api.com";
    private final String apiKey = "test-api-key";

    // 각 테스트 실행 전에 GeminiService 인스턴스를 초기화
    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(restTemplate, apiUrl, apiKey);
    }

    @Nested
    @DisplayName("generateText 메소드 테스트")
    class GenerateTextTest {

        @Test
        @DisplayName("API가 성공적으로 응답하면, 응답 텍스트를 반환해야 한다")
        void generateText_WhenApiReturnsSuccess_ShouldReturnText() {
            // given: 테스트를 위한 전제 조건 설정
            String prompt = "Hello, Gemini!";
            String expectedResponseText = "Hello, User!";

            // Mock GeminiResponse 객체 생성
            GeminiResponse mockResponse = new GeminiResponse();
            GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
            GeminiResponse.Content content = new GeminiResponse.Content();
            GeminiResponse.Part part = new GeminiResponse.Part();
            part.text = expectedResponseText;
            content.parts = Collections.singletonList(part);
            candidate.content = content;
            mockResponse.candidates = Collections.singletonList(candidate);

            // RestTemplate의 exchange 메소드가 ResponseEntity<GeminiResponse>를 반환하도록 mocking
            when(restTemplate.exchange(
                    eq(apiUrl + "?key=" + apiKey),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(GeminiResponse.class)
            )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

            // when: 테스트할 메소드 호출
            String actualResponse = geminiService.generateText(prompt);

            // then: 결과 검증
            assertThat(actualResponse).isEqualTo(expectedResponseText);
        }

        @Test
        @DisplayName("API 응답이 비어있거나 후보(candidates)가 없으면, '응답 없음'을 반환해야 한다")
        void generateText_WhenResponseIsEmpty_ShouldReturnNoResponse() {
            // given
            String prompt = "Hello, Gemini!";
            GeminiResponse mockResponse = new GeminiResponse();
            mockResponse.candidates = Collections.emptyList(); // 후보가 없는 응답

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(GeminiResponse.class)
            )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

            // when
            String actualResponse = geminiService.generateText(prompt);

            // then
            assertThat(actualResponse).isEqualTo("응답 없음");
        }
    }

    @Nested
    @DisplayName("classifyCategories 메소드 테스트")
    class ClassifyCategoriesTest {

        @Test
        @DisplayName("Gemini API가 분류된 카테고리 문자열을 반환하면, 이를 파싱하여 리스트로 반환해야 한다")
        void classifyCategories_WhenApiReturnsCategories_ShouldReturnParsedList() {
            // given
            Set<String> descriptions = Set.of("스타벅스", "배달의민족", "CGV");
            String geminiResponseText = "카페/간식, 식비, 문화/여가";

            // generateText 내부의 RestTemplate 호출을 mocking
            GeminiResponse mockResponse = new GeminiResponse();
            GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
            GeminiResponse.Content content = new GeminiResponse.Content();
            GeminiResponse.Part part = new GeminiResponse.Part();
            part.text = geminiResponseText;
            content.parts = Collections.singletonList(part);
            candidate.content = content;
            mockResponse.candidates = Collections.singletonList(candidate);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(GeminiResponse.class)
            )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

            // when
            List<String> classifiedCategories = geminiService.classifyCategories(descriptions);

            // then
            assertThat(classifiedCategories)
                    .isNotNull()
                    .hasSize(3)
                    .containsExactly("카페/간식", "식비", "문화/여가");
        }

        @Test
        @DisplayName("Gemini API가 빈 문자열을 반환하면, 빈 리스트를 반환해야 한다")
        void classifyCategories_WhenApiReturnsEmptyString_ShouldReturnEmptyList() {
            // given
            Set<String> descriptions = Set.of("스타벅스");
            String geminiResponseText = ""; // 빈 응답

            GeminiResponse mockResponse = new GeminiResponse();
            GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
            GeminiResponse.Content content = new GeminiResponse.Content();
            GeminiResponse.Part part = new GeminiResponse.Part();
            part.text = geminiResponseText;
            content.parts = Collections.singletonList(part);
            candidate.content = content;
            mockResponse.candidates = Collections.singletonList(candidate);

            when(restTemplate.exchange(
                    anyString(),
                    eq(HttpMethod.POST),
                    any(HttpEntity.class),
                    eq(GeminiResponse.class)
            )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

            // when
            List<String> classifiedCategories = geminiService.classifyCategories(descriptions);

            // then
            assertThat(classifiedCategories).isNotNull().isEmpty();
        }
    }
}
