# BankLab Backend
뱅크랩 서비스 백엔드 개발환경 구축 가이드입니다. 

## 기술 스택
- Framework : Spring 5.3
- Build : Gradle
- Server : Tomcat
- DB : MySQL + MyBatis
- Security : Spring Security 5.8
- Test : JUnit5

## 개발 환경 설정
### 사전 준비사항
- Tomcat Server 설치 (권장 9.0.105)
- Redis 설치 (v8.0.3)

### 설치 및 실행
1. **레포지토리 클론**
   ```bash
    git clone https://github.com/KB-ITYL-Banklab/Banklab-backend.git
    cd Banklab-backend
    ```
2. **IntelliJ IDE 실행**
	- 시작 시 Gradle 오류 - JDK 미설정으로 인한 오류이므로 JDK17로 설정한다.
	- `EnvFile` plugin 설치 
		![[Pasted image 20250814132623.png]]

	- `.env` 파일 설정
	- Tomcat 서버 아래쪽 화살표 Edit Configurations 클릭
	- EnvFile 탭에서 Enable EnvFile 체크, + 클릭 후 .env 파일 추가
	- Tomcat Deployment에서 `Artifact` 추가
	- Application context를 `/`로 변경 후 적용
3. **서버 실행**
	- Tomcat 서버 실행
	- `localhost:8080이 뜨면 성공
4. **redis 실행**
	- `redis-cli`후 `ping` 입력
	- `PONG` 응답이 오면 성공
	- 실행이 안될경우 C드라이브 - Program files - Redis로 들어가서 redis-cli.exe 실행
5. **Database 설정**
	- 우측 버튼에서 Database - Data Source - MySQL 연결
	- User/Password 설정 - 사용할 Database(예시 : `banklab`) 설정
	- Test Connection - 응답 후 적용
6. **Table 생성**
	- database0813.sql 실행
	- schema-mysql.sql 실행
	- mock_data.sql 실행
### 프론트 접속
1. 프론트 환경의 `localhost`로 접속한다. (`localhost:5173`)