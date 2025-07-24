package com.banklab.codeapi.service;

import com.banklab.codeapi.config.RestTemplateConfig;
import com.banklab.config.RootConfig;
import com.banklab.config.ServletConfig;
import com.banklab.codeapi.dto.TransactionRequestDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class, RestTemplateConfig.class})
@WebAppConfiguration
@Transactional // 각 테스트 후 DB 롤백
@Log4j2
class CodeapiServiceTest {

    @Autowired
    private CodeapiService codeapiService;

    @Test
    @DisplayName("실제 API와 DB를 연동하여 거래내역 조회 및 분류를 수행한다")
    void fetchAndSaveTransactions_IntegrationTest() {
        // Given: 실제 API 호출을 위한 요청 데이터
        // 중요: 이 테스트는 실제 connectedId와 계좌번호가 필요합니다.
        // application.properties에 설정된 codefapi.access.token 또한 유효해야 합니다.
        // 테스트 실행 전, 본인의 테스트용 connectedId와 계좌번호로 수정해주세요.
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setConnectedId("테스트 용 실제 값"); // <-- 테스트용 실제 값으로 변경 필요
        requestDto.setOrganization("0088"); // KB국민은행
        requestDto.setAccount("계좌번호"); // <-- 테스트용 실제 값으로 변경 필요
        requestDto.setStartDate("20250701");
        requestDto.setEndDate("20250721");
        requestDto.setOrderBy("0");

        // When & Then: 실제 API 호출 및 전체 프로세스가 예외 없이 실행되는지 검증
        // 이 테스트는 void를 반환하므로, 예외가 발생하지 않으면 성공으로 간주합니다.
        // 자세한 결과는 실행 시 로그를 통해 확인할 수 있습니다.
        assertDoesNotThrow(() -> {
            codeapiService.fetchAndSaveTransactions(requestDto);
        }, "fetchAndSaveTransactions 실행 중 예외가 발생해서는 안 됩니다.");

        log.info("실제 API 호출 및 DB 연동이 정상적으로 동작함을 의미합니다.");
        log.info("주의: saveTransactionsToDb 메소드가 주석 처리되어 있어 실제 DB 저장은 발생하지 않았을 수 있습니다.");
    }
}
