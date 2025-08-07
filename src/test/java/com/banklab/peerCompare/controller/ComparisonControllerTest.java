package com.banklab.peerCompare.controller;

import com.banklab.peerCompare.dto.PeerComparisonResponseDTO;
import com.banklab.peerCompare.service.ComparisonService;
import com.banklab.security.util.LoginUserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * JUnit5와 Mockito를 사용하여 ComparisonController를 단위 테스트하는 클래스입니다.
 * 이 클래스는 실제 웹 서버를 실행하지 않고 컨트롤러의 로직이 올바르게 동작하는지 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
public class ComparisonControllerTest {

    // @Mock: ComparisonService의 모의(가짜) 객체를 생성합니다. 컨트롤러가 의존하는 서비스 로직을 시뮬레이션합니다.
    @Mock
    private ComparisonService comparisonService;

    // @Mock: LoginUserProvider의 모의 객체를 생성합니다. 사용자 인증 정보를 제공하는 로직을 시뮬레이션합니다.
    @Mock
    private LoginUserProvider loginUserProvider;

    // @InjectMocks: 위에서 @Mock으로 생성된 모의 객체들을 실제 테스트 대상인 ComparisonController에 주입합니다.
    @InjectMocks
    private ComparisonController comparisonController;

    /**
     * 또래 비교 API가 성공적으로 호출되었을 때의 시나리오를 테스트합니다.
     * - given: 로그인된 사용자가 존재하고, 서비스가 정상적으로 또래 비교 데이터를 반환하는 상황을 설정합니다.
     * - when: 컨트롤러의 getPeerComparison 메서드를 직접 호출합니다.
     * - then: HTTP 응답 상태 코드가 200 OK이고, 응답 본문에 예상된 데이터가 포함되어 있는지 검증합니다.
     */
    @Test
    @DisplayName("또래 비교 API 성공 테스트")
    void getPeerComparison_success() {
        // given: 테스트에 필요한 상황을 설정합니다.
        Long memberId = 1L;
        String email = "test@example.com";

        // loginUserProvider가 로그인된 사용자 정보를 반환하도록 설정합니다.
        when(loginUserProvider.getLoginMemberId()).thenReturn(memberId);
        when(loginUserProvider.getLoginEmail()).thenReturn(email);

        // comparisonService가 반환할 또래 비교 응답 DTO를 생성합니다.
        PeerComparisonResponseDTO responseDTO = PeerComparisonResponseDTO.builder()
                .peerAvgTotalExpense(100000L)
                .categoryComparisons(Collections.emptyList())
                .build();

        // comparisonService.getPeerCategoryCompare 메서드가 호출되면, 위에서 생성한 responseDTO를 반환하도록 설정합니다.
        when(comparisonService.getPeerCategoryCompare(any(), any(), any(), any())).thenReturn(responseDTO);

        // when: 실제 테스트할 컨트롤러 메서드를 호출하고, 그 결과를 ResponseEntity 객체로 받습니다.
        ResponseEntity<Map<String, Object>> response = comparisonController.getPeerComparison(null, null);

        // then: 반환된 결과를 검증합니다.
        // HTTP 상태 코드가 200 OK인지 확인합니다.
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // 응답 본문을 Map 형태로 가져옵니다.
        Map<String, Object> body = response.getBody();
        // 본문에서 "data" 키에 해당하는 값을 가져옵니다.
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        // "data" 맵에서 "peer" 키에 해당하는 값을 PeerComparisonResponseDTO로 형변환합니다.
        PeerComparisonResponseDTO peer = (PeerComparisonResponseDTO) data.get("peer");
        // 최종적으로 또래 평균 총 지출액이 예상(100000L)과 같은지 확인합니다.
        assertEquals(100000L, peer.getPeerAvgTotalExpense());
    }

    /**
     * 인증되지 않은 사용자가 또래 비교 API를 호출했을 때의 시나리오를 테스트합니다.
     * - given: 로그인된 사용자가 없는 상황을 설정합니다.
     * - when: 컨트롤러의 getPeerComparison 메서드를 직접 호출합니다.
     * - then: HTTP 응답 상태 코드가 401 Unauthorized이고, 적절한 에러 메시지를 포함하는지 검증합니다.
     */
    @Test
    @DisplayName("또래 비교 API 인증 실패 테스트")
    void getPeerComparison_unauthorized() {
        // given: 로그인된 사용자가 없는 상황을 시뮬레이션합니다.
        // loginUserProvider가 null을 반환하도록 설정하여, 로그인되지 않은 상태를 만듭니다.
        when(loginUserProvider.getLoginMemberId()).thenReturn(null);
        when(loginUserProvider.getLoginEmail()).thenReturn(null);

        // when: 컨트롤러 메서드를 호출합니다.
        ResponseEntity<Map<String, Object>> response = comparisonController.getPeerComparison(null, null);

        // then: 반환된 결과를 검증합니다.
        // HTTP 상태 코드가 401 Unauthorized인지 확인합니다.
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        // 응답 본문을 가져옵니다.
        Map<String, Object> body = response.getBody();
        // 본문의 "message" 키 값이 예상된 에러 메시지와 같은지 확인합니다.
        assertEquals("인증이 필요합니다. 로그인 후 다시 시도하세요", body.get("message"));
        // 본문의 "error" 키 값이 "AUTHENTICATION_ERROR"인지 확인합니다.
        assertEquals("AUTHENTICATION_ERROR", body.get("error"));
    }
}