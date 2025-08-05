-- 업비트 테이블 가격 컬럼을 소수점 둘째 자리까지로 변경

-- 기존 테이블 백업 (선택사항)
-- CREATE TABLE finance_upbit_backup AS SELECT * FROM finance_upbit;

-- 컬럼 타입 변경 (가격 관련 필드들을 DECIMAL(20,2)로 변경)
ALTER TABLE finance_upbit 
MODIFY COLUMN opening_price DECIMAL(20,2) COMMENT '시가 (원화, 소수점 2자리)',
MODIFY COLUMN trade_price DECIMAL(20,2) COMMENT '종가 (현재가, 원화, 소수점 2자리)',
MODIFY COLUMN prev_closing_price DECIMAL(20,2) COMMENT '전일 종가 (원화, 소수점 2자리)';

-- 변경된 테이블 구조 확인
DESCRIBE finance_upbit;

-- 기존 데이터 삭제 (새로운 정확한 데이터로 다시 수집)
-- TRUNCATE TABLE finance_upbit;
