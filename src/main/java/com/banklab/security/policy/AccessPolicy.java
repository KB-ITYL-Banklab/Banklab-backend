package com.banklab.security.policy;

import org.springframework.http.HttpMethod;

import java.util.List;

public class AccessPolicy {

    // permitAll 리스트
    public static final List<AccessRule> PERMIT_ALL = List.of(
            // 금융 API 공개 GET
            new AccessRule(HttpMethod.GET, "/api/news/**", null),   // 경제뉴스 API 공개
            new AccessRule(HttpMethod.GET, "/api/gold/**", null),   // 금융차트 금 API 공개
            new AccessRule(HttpMethod.GET, "/api/stocks/**", null), // 금융차트 주식 API 공개
            new AccessRule(HttpMethod.GET, "/api/upbit/**", null),  // 금융차트 가상화페 API 공개
            new AccessRule(HttpMethod.GET, "/api/exchange/**", null),   // 금융차트 외환 API 공개
            new AccessRule(HttpMethod.GET, "/api/health/**", null),     // 금융차트  API 연결여부 공개
            new AccessRule(HttpMethod.GET, "/api/terms/**", null),      // 금융용어  API  공개

            // 회원 관련
            new AccessRule(HttpMethod.POST, "/api/member", null), // 회원가입
            new AccessRule(HttpMethod.GET, "/api/member/exist/email/**", null), // 이메일 중복 체크
            new AccessRule(HttpMethod.POST, "/api/verification/**", null), // 이메일/전화번호 인증
            new AccessRule(HttpMethod.POST, "/api/member/find/**", null), // 아이디 찾기
            new AccessRule(HttpMethod.POST, "/api/member/password/reset", null), // 비밀번호 재설정

            // 인증 토큰
            new AccessRule(HttpMethod.POST, "/api/auth/reissue", null), // 토큰 재발급
            new AccessRule(HttpMethod.POST, "/api/auth/logout", null),  // 로그아웃

            // 소셜 로그인
            new AccessRule(null, "/login/**", null),
            new AccessRule(null, "/oauth2/**", null),
            new AccessRule(null, "/login/oauth2/**", null)
    );

    // 인증 필요 (명시적으로 지정 필요 시 사용)
//    public static final List<AccessRule> AUTHENTICATED = List.of(
//            new AccessRule(HttpMethod.POST, "/api/member/update", "AUTHENTICATED")
//    );

    // 역할 구분 필요시 사용 (참고용)
//    public static final List<AccessRule> ADMIN_ONLY = List.of(
//            new AccessRule(HttpMethod.GET, "/api/admin/**", "ROLE_ADMIN")
//    );

    public static class AccessRule {
        public final HttpMethod method;
        public final String uriPattern;
        public final String role; // null: permitAll, "AUTHENTICATED", "ROLE_ADMIN"

        public AccessRule(HttpMethod method, String uriPattern, String role) {
            this.method = method;
            this.uriPattern = uriPattern;
            this.role = role;
        }
    }
}
