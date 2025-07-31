package com.banklab.oauth.service;

import com.banklab.member.mapper.MemberMapper;
import com.banklab.member.service.MemberService;
import com.banklab.oauth.client.KakaoOAuthClient;
import com.banklab.oauth.domain.OAuthProvider;
import com.banklab.oauth.dto.KakaoUserInfoDTO;
import com.banklab.security.account.domain.MemberVO;
import com.banklab.security.account.dto.AuthResultDTO;
import com.banklab.security.account.dto.UserInfoDTO;
import com.banklab.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {
    private final KakaoOAuthClient kakaoOAuthClient;
    private final MemberMapper memberMapper;
    private final JwtProcessor jwtProcessor;
    private final MemberService memberService;

    public AuthResultDTO login(String code) {
        String accessToken = kakaoOAuthClient.getToken(code).getAccessToken();
        KakaoUserInfoDTO userInfo = kakaoOAuthClient.getUserInfo(accessToken);

        // 기존 회원 확인
        MemberVO member = memberMapper.findByProviderAndProviderId(OAuthProvider.KAKAO, userInfo.getId());
        // 회원이 없으면 → 최초 로그인
        if (member == null) {
            member = userInfo.toVO();
            member = memberService.registerMember(member).toVO();
        } else {
            // 기존 회원이라도 → 전화번호 등이 바뀌었을 수 있음
            // → 필요하면 업데이트
        }

        String token = jwtProcessor.generateTokenWithId(member.getEmail(), member.getMemberId());

        return new AuthResultDTO(token, UserInfoDTO.of(member));
    }
}
