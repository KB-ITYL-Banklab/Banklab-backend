package com.banklab.codef.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/**
 *	Codef 공식 상수 클래스
 *  @Param : API 요청 URL
 */
@Component
@PropertySource("classpath:application-secret.properties")
public class CommonConstant {
    public static final String API_DOMAIN 	= "https://api.codef.io";										// API서버 도메인
    public static final String TEST_DOMAIN 	= "https://development.codef.io";								// API서버 데모 도메인

    public static final String TOKEN_DOMAIN = "https://oauth.codef.io";										// OAUTH2.0 테스트 도메인
    public static final String GET_TOKEN 	= "/oauth/token";												// OAUTH2.0 토큰 발급 요청 URL

    public static final String CONNECTED_ID = "connectedId";												// 유저 식별 연결 아이디
    public static final String PAGE_NO 		= "pageNo";														// 페이지 번호

    public static final String KR_BK_1_B_001	= "/v1/kr/bank/b/account/account-list";                     // 은행 기업 보유계좌
    public static final String KR_BK_1_B_002	= "/v1/kr/bank/b/account/transaction-list";                 // 은행 기업 수시입출 거래내역
    public static final String KR_BK_1_B_003	= "/v1/kr/bank/b/installment-savings/transaction-list";     // 은행 기업 적금 거래내역
    public static final String KR_BK_1_B_004	= "/v1/kr/bank/b/loan/transaction-list";                    // 은행 기업 대출 거래내역
    public static final String KR_BK_1_B_005	= "/v1/kr/bank/b/exchange/transaction-list";                // 은행 기업 외화 거래내역
    public static final String KR_BK_1_B_006	= "/v1/kr/bank/b/fund/transaction-list";                    // 은행 기업 펀드 거래내역
    public static final String KR_BK_1_B_007	= "/v1/kr/bank/b/fast-account/transaction-list";            // 은행 기업 빠른계좌조회

    public static final String KR_BK_1_P_001	= "/v1/kr/bank/p/account/account-list";                     // 은행 개인 보유계좌
    public static final String KR_BK_1_P_002	= "/v1/kr/bank/p/account/transaction-list";                 // 은행 개인 수시입출 거래내역
    public static final String KR_BK_1_P_003	= "/v1/kr/bank/p/installment-savings/transaction-list";     // 은행 개인 적금 거래내역
    public static final String KR_BK_1_P_004	= "/v1/kr/bank/p/loan/transaction-list";                    // 은행 개인 대출 거래내역
    public static final String KR_BK_1_P_005	= "/v1/kr/bank/p/fast-account/transaction-list";            // 은행 개인 빠른계좌조회

    public static final String KR_BK_2_P_001	= "/v1/kr/bank2/p/account/account-list";                    // 저축은행 개인 보유계좌 조회
    public static final String KR_BK_2_P_002	= "/v1/kr/bank2/p/account/transaction-list";                // 저축은행 개인 수시입출 거래내역

    public static final String KR_CD_B_001	= "/v1/kr/card/b/account/card-list";                            // 카드 법인 보유카드
    public static final String KR_CD_B_002	= "/v1/kr/card/b/account/approval-list";                        // 카드 법인 승인내역
    public static final String KR_CD_B_003	= "/v1/kr/card/b/account/billing-list";                         // 카드 법인 청구내역
    public static final String KR_CD_B_004	= "/v1/kr/card/b/account/limit";                                // 카드 법인 한도조회

    public static final String KR_CD_P_001	= "/v1/kr/card/p/account/card-list";                            // 카드 개인 보유카드
    public static final String KR_CD_P_002	= "/v1/kr/card/p/account/approval-list";                        // 카드 개인 승인내역
    public static final String KR_CD_P_003	= "/v1/kr/card/p/account/billing-list";                         // 카드 개인 청구내역
    public static final String KR_CD_P_004	= "/v1/kr/card/p/account/limit";                                // 카드 개인 한도조회

    public static final String KR_PB_NT_001	= "/v1/kr/public/nt/business/status";                            // 공공 사업자상태
    public static final String KR_PB_CK_001	= "/v1/kr/public/ck/real-estate-register/status";                // 공공 부동산등기
    public static final String KR_PB_EF_001	= "/v1/kr/public/ef/driver-license/status";                      // 공공 운전면허 진위여부
    public static final String KR_PB_MW_001	= "/v1/kr/public/mw/identity-card/status";                       // 공공 주민등록 진위여부

    public static final String KR_IS_0001_001	= "/v1/kr/insurance/0001/credit4u/contract-info";     	  	// 보험다보여-계약정보조회
    public static final String KR_IS_0001_002	= "/v1/kr/insurance/0001/credit4u/register";     	  		// 보험다보여-회원가입신청
    public static final String KR_IS_0001_003	= "/v1/kr/insurance/0001/credit4u/find-id";     	  		// 보험다보여-아이디찾기
    public static final String KR_IS_0001_004	= "/v1/kr/insurance/0001/credit4u/change-pwd";     	  		// 보험다보여-비밀번호변경
    public static final String KR_IS_0001_005	= "/v1/kr/insurance/0001/credit4u/unregister";     	  		// 보험다보여-회원탈퇴신청


    public static final String GET_CONNECTED_IDS = "/v1/account/connectedId-list";       					// 커넥티드아이디 목록 조회
    public static final String GET_ACCOUNTS = "/v1/account/list";            								// 계정 목록 조회
    public static final String CREATE_ACCOUNT = "/v1/account/create";            							// 계정 등록(커넥티드아이디 발급)
    public static final String ADD_ACCOUNT = "/v1/account/add";            									// 계정 추가
    public static final String UPDATE_ACCOUNT = "/v1/account/update";            							// 계정 수정
    public static final String DELETE_ACCOUNT = "/v1/account/delete";            							// 계정 삭제

    /**
     * API 요청 도메인 반환
     * @return
     */
    public static String getRequestDomain() {
        return CommonConstant.TEST_DOMAIN;
    }


    /**	CODEF로부터 발급받은 클라이언트 아이디	*/
    @Value("${codef.client.id}")
    public static String CLIENT_ID="46c0a2da-ded2-4466-9440-dbf21e6699a3";

    /**	CODEF로부터 발급받은 시크릿 키	*/
    @Value("${codef.client.secret}")
    public static String SECERET_KEY="cd812294-063b-4429-822f-cc562f551d06";

    /**	CODEF로부터 발급받은 퍼블릭 키	*/
    @Value("${codef.public.key}")
    public static String PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5rr2LVqPWyNCSWFN4r6NQ4f7TLWE1ibn+NAXye6W2xRXdwYNHNXuAVIUZzDIlNaPnKQtV/tqFEgCak6wx1rldcx7ohQxoOSuNlQvCbEaNqu6Iqkz7frpcYZZ3GetJ9A7HUBfqBsuUBWzlbr2XVyiPTRw46StTAb/wauoGMk3AVjlR5XUrnIGvGP6AepKzgTn2F8fEsP011hlW6gzWW7TSWGNIO/YTGSqFA9jgSX0IMO8O2pEyRV7p+PM+66q0dKq4gwGak7YaAzxHqnfIBy4eQ7QQBYCW5gkFT3H5QYdZP0zMKHXAKLl2q2ghoh29tpw5J9rEJy8z8c34y2TJry0pQIDAQAB";

    /**	OAUTH2.0 토큰 샘플	*/
    @Value("${codef.access.token}")
    public static String ACCESS_TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXJ2aWNlX3R5cGUiOiIxIiwic2NvcGUiOlsicmVhZCJdLCJzZXJ2aWNlX25vIjoiMDAwMDA1NzY1MDAyIiwiZXhwIjoxNzUzNjY2NTAxLCJhdXRob3JpdGllcyI6WyJJTlNVUkFOQ0UiLCJQVUJMSUMiLCJCQU5LIiwiRVRDIiwiU1RPQ0siLCJDQVJEIl0sImp0aSI6IjBkMDdhY2EzLTNkNWEtNDRmZS1hMDI1LWEzM2U2ZGM4ZDA4ZCIsImNsaWVudF9pZCI6IjQ2YzBhMmRhLWRlZDItNDQ2Ni05NDQwLWRiZjIxZTY2OTlhMyJ9.f3uPAw6PHxBcXiRznrzOrd3A_vSzxL40TTUVCuL5vAScwJ8NAeNAj_4l1gxTfIJ4gHWL15vCQaObNE96QyWXL0O3QSPLFs_rvqd9fLOz5A6zYs6eyjD5wCeOYmtoAAClHVGkfnGxfOhnsnGVW436rnolcSdwR1EdE7ujSSQ0a__4zgvfwbWUtSJekArHX0OValZOC9Cjtk-cJGfc_qdBbGh0Sou2h2iwMQ2sRDS4B5rCXxivuOWfzQWZUeQ9biwSR20IQ-PI4Zm_p8-PG6AQf0-sc-6PxxCtLdnttK109yWG_XshdcfhZFc8xLwxRHyOvcBibZOcM6cnq8YhQuqw2A";



}
