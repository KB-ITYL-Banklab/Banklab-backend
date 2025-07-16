package com.banklab.codef.util;

/**
 *	Codef 공식 상수 클래스
 *  @Param : API 요청 URL
 */
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
    public static final String CLIENT_ID = "6675c900-d0bf-4fd1-ba20-28387b613835";

    /**	CODEF로부터 발급받은 시크릿 키	*/
    public static final String SECERET_KEY = "5ca86438-4597-4878-b6bb-781f4100ce2f";

    /**	CODEF로부터 발급받은 퍼블릭 키	*/
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjyupOLcYQ2/0OjWyYjyPq9v1/X0bx9tUdGe6s7sMGCEW7u+J6jdiK/sgJRRNdcPYLrAkRY1HQPvZmmdc25oizX1zLFnq2RDUW0VH4ec41VYG5NOYAQfJiY8sJtouas/JNh0uXfQGp2i7WW6NtLAQgaeymEEU+YdTeXpeIQuKcLPjnwLPaejwFM4Tb7hlzzhin74tjf2xJQKqfieS5+q+l1Zyze1JNE+BlzhMOZ1T1b6K1umChc04bt0+nflgpvT2GF2HXaYCJ8x7OQmtN54EdFPOywEfjtxiKCxT82wfPpdb8/KmjM4xIMFvywHlen/gUglaGVzN7L2jTMXsI6ey6wIDAQAB";

    /**	OAUTH2.0 토큰 샘플	*/
    public static String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZXJ2aWNlX3R5cGUiOiIxIiwic2NvcGUiOlsicmVhZCJdLCJzZXJ2aWNlX25vIjoiMDAwMDA1NzU4MDAyIiwiZXhwIjoxNzUzMTcxMDYxLCJhdXRob3JpdGllcyI6WyJJTlNVUkFOQ0UiLCJQVUJMSUMiLCJCQU5LIiwiRVRDIiwiU1RPQ0siLCJDQVJEIl0sImp0aSI6IjY2ZmUxNTMzLTg4NzgtNGUyZS1hYWIwLTZlODZjYTNiODJjMiIsImNsaWVudF9pZCI6IjY2NzVjOTAwLWQwYmYtNGZkMS1iYTIwLTI4Mzg3YjYxMzgzNSJ9.YUFVOEhrYa31NQqkYDrddExwm4djX2KR4hRXkKGfyT3LWzJsfRqI-Ub95H_2yPwP8eL9D-TWZgmdA3nnusMIfUFRP5lkvYx-eEPhqIHaS0POKrW4e7bzdRFQlBaXjt9b-TfUv7X4VcyWs98qYo7fBdpN1rMwSJazgZgAQ0af8EUj2A5GSAwQzxUB3KbikU9GHkkvbSnsbxsnSwP4BSrpcu_OmU2Ux4O0wivafC2MsezA0uCkaSQXcZ8VtiMhh4rPJQIo-7JPAPEzPkbheRifFw6AS6nIRPehHq4QWH0Y6EUMx19zE9R81myL-IGyeOY35Xpb1Hxs05dQ2flEw02OJg";

}
