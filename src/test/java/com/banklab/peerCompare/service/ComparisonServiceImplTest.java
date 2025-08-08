package com.banklab.peerCompare.service;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.service.MemberService;
import com.banklab.peerCompare.dto.CategoryComparisonDTO;
import com.banklab.peerCompare.dto.PeerComparisonResponseDTO;
import com.banklab.peerCompare.mapper.ComparisonMapper;
import com.banklab.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * JUnit5와 Mockito를 사용하여 ComparisonService의 구현체인 ComparisonServiceImpl을 테스트하는 클래스입니다.
 * 이 클래스는 또래 비교 로직이 올바르게 동작하는지 단위 테스트를 통해 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
public class ComparisonServiceImplTest {

    // @Mock: ComparisonMapper의 모의(가짜) 객체를 생성합니다. 실제 데이터베이스에 접근하지 않고, 미리 정의된 값을 반환하도록 설정할 수 있습니다.
    @Mock
    private ComparisonMapper comparisonMapper;

    // @Mock: MemberService의 모의 객체를 생성합니다. 회원 정보를 조회하는 로직을 시뮬레이션합니다.
    @Mock
    private MemberService memberService;

    // @Mock: TransactionService의 모의 객체를 생성합니다. 이 테스트에서는 사용되지 않지만, ComparisonServiceImpl의 의존성이므로 모의 객체로 생성합니다.
    @Mock
    private TransactionService transactionService;

    // @InjectMocks: 위에서 @Mock으로 생성된 모의 객체들을 실제 테스트 대상인 ComparisonServiceImpl에 주입합니다.
    @InjectMocks
    private ComparisonServiceImpl comparisonService;

    // 테스트에서 공통으로 사용될 변수들을 선언합니다.
    private MemberDTO testMember;
    private Long memberId;
    private String email;
    private Date startDate;
    private Date endDate;

    /**
     * 각 테스트 메서드가 실행되기 전에(@BeforeEach) 공통적으로 필요한 설정 작업을 수행합니다.
     * 테스트용 회원 정보와 조회 기간을 초기화합니다.
     */
    @BeforeEach
    void setUp() {
        memberId = 1L;
        email = "test@example.com";
        testMember = new MemberDTO();
        testMember.setBirth("1990-01-01"); // 테스트 회원의 생년월일을 설정합니다. (나이 계산에 사용)

        LocalDate now = LocalDate.now();
        startDate = java.sql.Date.valueOf(now.withDayOfMonth(1)); // 조회 시작일을 현재 달의 1일로 설정
        endDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth())); // 조회 종료일을 현재 달의 마지막 날로 설정
    }

    /**
     * 또래 그룹의 카테고리별 소비 데이터를 가져오는 getPeerCategoryCompare 메서드의 정상 동작을 테스트합니다.
     * - given: 테스트에 필요한 상황(회원 정보, 또래 소비 데이터 등)을 설정합니다.
     * - when: 실제 테스트할 메서드를 호출합니다.
     * - then: 메서드 호출 결과가 예상과 일치하는지 검증합니다.
     */
    @Test
    @DisplayName("또래 카테고리별 소비 비교 테스트")
    void getPeerCategoryCompareTest() {
        // given: 모의 객체들이 특정 조건에서 어떻게 동작할지 정의합니다.
        // memberService.get 메서드가 호출되면, 미리 준비된 testMember 객체를 반환하도록 설정합니다.
        when(memberService.get(memberId, email)).thenReturn(testMember);

        // 테스트용 또래 카테고리별 지출 데이터를 생성합니다.
        CategoryComparisonDTO category1 = new CategoryComparisonDTO();
        category1.setCategoryId(1L);
        category1.setCategoryName("식비");
        category1.setAvgExpense(50000.0);

        CategoryComparisonDTO category2 = new CategoryComparisonDTO();
        category2.setCategoryId(2L);
        category2.setCategoryName("교통");
        category2.setAvgExpense(30000.0);

        List<CategoryComparisonDTO> peerCategoryExpense = Arrays.asList(category1, category2);
        // comparisonMapper.getPeerCategoryExpense 메서드가 호출되면, 위에서 생성한 peerCategoryExpense 리스트를 반환하도록 설정합니다.
        when(comparisonMapper.getPeerCategoryExpense(anyLong(), any(Date.class), any(Date.class), anyInt(), anyInt()))
                .thenReturn(peerCategoryExpense);

        // 또래의 평균 총 지출액을 80000L로 설정합니다.
        Long peerAvgTotalExpense = 80000L;
        // comparisonMapper.getPeerTotalAvgExpense 메서드가 호출되면, peerAvgTotalExpense 값을 반환하도록 설정합니다.
        when(comparisonMapper.getPeerTotalAvgExpense(anyLong(), any(Date.class), any(Date.class), anyInt(), anyInt()))
                .thenReturn(peerAvgTotalExpense);

        // when: 실제 테스트 대상 메서드인 getPeerCategoryCompare를 호출하고, 그 결과를 받습니다.
        PeerComparisonResponseDTO result = comparisonService.getPeerCategoryCompare(memberId, email, startDate, endDate);

        // then: 반환된 결과(result)가 예상과 일치하는지 검증합니다.
        // 또래 평균 총 지출액이 예상(80000L)과 같은지 확인합니다.
        assertEquals(peerAvgTotalExpense, result.getPeerAvgTotalExpense());
        // 카테고리 비교 결과의 개수가 2개인지 확인합니다.
        assertEquals(2, result.getCategoryComparisons().size());
        // 정렬 후 첫 번째 카테고리의 이름이 "식비"인지 확인합니다. (ComparisonServiceImpl 정렬 로직에 따라 avgExpense가 높은 순)
        assertEquals("식비", result.getCategoryComparisons().get(0).getCategoryName());
    }
}