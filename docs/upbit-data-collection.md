# 업비트 데이터 수집 시스템

## 개요
업비트 API를 활용하여 가상화폐 시세 정보를 자동으로 수집하고 데이터베이스에 저장하는 시스템입니다.

## 기능
- 업비트 API를 통한 실시간 시세 데이터 수집
- 모든 KRW 마켓의 데이터 자동 수집
- 일일 단위 스케줄링 (매일 오전 9시)
- 데이터 중복 방지 (같은 날짜 데이터 업데이트)
- REST API를 통한 수동 실행 및 조회

## 데이터베이스 테이블 구조

```sql
CREATE TABLE finance_upbit (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '고유 ID',
    market VARCHAR(30) NOT NULL COMMENT '마켓 코드 (예: KRW-BTC)',
    opening_price DOUBLE COMMENT '시가',
    trade_price DOUBLE COMMENT '종가 (현재가)',
    prev_closing_price DOUBLE COMMENT '전일 종가',
    change_rate DOUBLE COMMENT '전일 대비 등락률 (비율)',
    acc_trade_volume_24h DOUBLE COMMENT '24시간 누적 거래량',
    acc_trade_price_24h DOUBLE COMMENT '24시간 누적 거래대금',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 생성 시각',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '데이터 수정 시각'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='가상화폐 일별 시세 정보 (업비트)';
```

## 주요 컴포넌트

### 1. DTO (Data Transfer Object)
- `UpbitTickerDto`: 업비트 Ticker API 응답 매핑
- `UpbitMarketDto`: 업비트 마켓 정보 매핑

### 2. Domain
- `FinanceUpbit`: 데이터베이스 테이블과 매핑되는 도메인 객체

### 3. Service
- `UpbitApiService`: 업비트 API 호출 담당
- `UpbitDataService`: 데이터 수집, 변환, 저장 담당

### 4. Scheduler
- `UpbitDataScheduler`: 자동 데이터 수집 스케줄링

### 5. Controller
- `UpbitController`: REST API 엔드포인트 제공

### 6. Mapper
- `UpbitMapper`: MyBatis를 통한 데이터베이스 접근

## API 엔드포인트

### 데이터 수집
```http
POST /api/upbit/collect
```
업비트 데이터를 수동으로 수집합니다.

### 특정 마켓 조회
```http
GET /api/upbit/latest/{market}
```
특정 마켓 코드의 최신 데이터를 조회합니다.
- 예: `/api/upbit/latest/KRW-BTC`

### 전체 마켓 조회
```http
GET /api/upbit/latest/all
```
모든 마켓의 최신 데이터를 조회합니다.

### 헬스 체크
```http
GET /api/upbit/health
```
시스템 상태를 확인합니다.

## 스케줄링
- **기본 스케줄**: 매일 오전 9시 자동 실행
- **테스트 스케줄**: 10분마다 실행 (주석 처리됨)
- **수동 실행**: REST API를 통한 즉시 실행

## 설정

### 스케줄링 활성화
`RootConfig.java`에서 `@EnableScheduling` 어노테이션으로 활성화됩니다.

### 컴포넌트 스캔 경로
```java
@ComponentScan(basePackages = {
    "com.banklab.financeContents.service",
    "com.banklab.financeContents.scheduler"
})
```

## 사용 방법

### 1. 수동 데이터 수집
```bash
curl -X POST http://localhost:8080/api/upbit/collect
```

### 2. 데이터 조회
```bash
# 비트코인 데이터 조회
curl http://localhost:8080/api/upbit/latest/KRW-BTC

# 전체 데이터 조회
curl http://localhost:8080/api/upbit/latest/all
```

## 로그
- 데이터 수집 시작/완료 로그
- 각 마켓별 처리 상태 (DEBUG 레벨)
- 오류 발생 시 상세 에러 로그

## 주의사항
1. 업비트 API 호출 제한을 준수합니다
2. 같은 날짜에 데이터가 있으면 업데이트, 없으면 삽입합니다
3. KRW 마켓만 수집합니다 (BTC, USDT 마켓 제외)
4. 네트워크 오류 시 재시도 로직이 포함되어 있습니다

## 테스트
- `UpbitApiServiceTest`: API 호출 테스트
- `UpbitDataSchedulerTest`: 전체 데이터 수집 테스트

## 향후 개선사항
- 캔들 데이터 수집 기능 추가
- 실시간 WebSocket 연동
- 데이터 백업 및 복구 기능
- 모니터링 및 알림 기능
