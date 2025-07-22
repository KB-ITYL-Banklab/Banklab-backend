# 공공데이터포털 주식 정보 API 연동 가이드

## 개요
이 프로젝트는 Spring Legacy 프레임워크를 사용하여 공공데이터포털의 주식 정보 API를 연동합니다.

## API 정보
- **API 제공기관**: 공공데이터포털 (data.go.kr)
- **API명**: 주식시세정보
- **서비스URL**: https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService

## 구현된 기능

### 1. 서비스 클래스
- `PublicDataStockService`: 공공데이터 API 호출 및 데이터 처리

### 2. DTO 클래스
- `StockSecurityInfoDto`: 주식 정보 데이터 매핑
- `StockApiResponseDto`: API 응답 전체 구조 매핑

### 3. 컨트롤러 엔드포인트

#### 3.1 특정 종목 조회
```
GET /api/stocks/public/{stockCode}
```
- **파라미터**: stockCode (종목 단축코드 6자리)
- **예시**: `/api/stocks/public/005930` (삼성전자)

#### 3.2 주식 목록 조회
```
GET /api/stocks/public/list
```
- **파라미터**:
  - `baseDate`: 기준일자 (YYYYMMDD, 선택사항)
  - `numOfRows`: 조회할 종목 수 (기본값: 10)
  - `pageNo`: 페이지 번호 (기본값: 1)

#### 3.3 주요 종목 조회
```
GET /api/stocks/public/top/{count}
```
- **파라미터**: count (조회할 종목 수, 최대 100)

### 4. 테스트 엔드포인트

#### 4.1 API 설정 확인
```
GET /api/test/config
```

#### 4.2 샘플 데이터 조회
```
GET /api/test/stock/sample
```

#### 4.3 상위 5개 종목 조회
```
GET /api/test/stock/top5
```

## 설정 방법

### 1. API 키 설정
`src/main/resources/application.properties` 파일에 API 키를 설정합니다:

```properties
# Stock API
stock.api.url=https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo
stock.api.key=[귀하의_API_키]
```

### 2. 의존성
이미 `build.gradle`에 다음 의존성이 포함되어 있습니다:
- Apache HttpComponents (HTTP 클라이언트)
- Jackson (JSON 처리)
- Spring Web (웹 MVC)

## 사용 예시

### Java 코드에서 사용
```java
@Autowired
private PublicDataStockService stockService;

// 삼성전자 주식 정보 조회
StockSecurityInfoDto samsung = stockService.getStockInfoByCode("005930");

// 상위 10개 종목 조회
List<StockSecurityInfoDto> topStocks = stockService.getTopStocks(10);
```

### HTTP API 호출
```bash
# 삼성전자 정보 조회
curl -X GET "http://localhost:8080/api/stocks/public/005930"

# 상위 5개 종목 조회
curl -X GET "http://localhost:8080/api/stocks/public/top/5"

# 목록 조회 (페이징)
curl -X GET "http://localhost:8080/api/stocks/public/list?numOfRows=20&pageNo=1"
```

## 주요 종목 코드
- 삼성전자: 005930
- SK하이닉스: 000660
- 네이버: 035420
- 카카오: 035720
- LG에너지솔루션: 373220

## 응답 데이터 구조
```json
{
  "baseDate": "20240121",
  "shortCode": "005930",
  "itemName": "삼성전자",
  "marketCategory": "KOSPI",
  "closePrice": "75000",
  "versus": "1000",
  "fluctuationRate": "1.35",
  "marketPrice": "74500",
  "highPrice": "75500",
  "lowPrice": "74000",
  "tradingQuantity": "15000000",
  "tradingPrice": "1125000000000"
}
```

## 에러 처리
- API 키가 잘못된 경우: HTTP 401 Unauthorized
- 네트워크 연결 오류: HTTP 500 Internal Server Error
- 존재하지 않는 종목 코드: HTTP 404 Not Found
- API 서버 오류: HTTP 500 Internal Server Error

모든 에러는 적절한 HTTP 상태 코드와 함께 로그에 기록됩니다.

## 로그 설정
API 호출 과정을 추적하기 위해 다음과 같은 로그가 출력됩니다:
- API 호출 URL
- 응답 상태 코드
- 응답 데이터 (DEBUG 레벨)

## 테스트 방법

### 1. 단위 테스트
```bash
./gradlew test --tests "com.banklab.financeContents.service.PublicDataStockServiceTest"
```

### 2. 통합 테스트
서버를 실행한 후 브라우저나 Postman에서 테스트 엔드포인트에 접근:
- http://localhost:8080/api/test/config
- http://localhost:8080/api/test/stock/sample

## 주의사항
1. 공공데이터포털 API는 일일 호출 제한이 있습니다.
2. API 키는 공개되지 않도록 주의해야 합니다.
3. 주식 시장이 열리지 않은 시간에는 최신 데이터가 아닐 수 있습니다.
4. API 응답 시간이 느릴 수 있으므로 적절한 타임아웃 설정이 필요합니다.

## 향후 개선 사항
1. 캐싱 기능 추가
2. 비동기 처리 개선
3. 에러 처리 고도화
4. 데이터 저장 기능 추가
5. 실시간 주식 데이터 연동
