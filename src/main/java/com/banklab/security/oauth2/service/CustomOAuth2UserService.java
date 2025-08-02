package com.banklab.security.oauth2.service;

import com.banklab.member.domain.Gender;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.security.oauth2.domain.OAuthProvider;
import com.banklab.security.account.domain.AuthVO;
import com.banklab.security.oauth2.domain.CustomOAuth2User;
import com.banklab.security.account.domain.MemberVO;
import com.banklab.security.oauth2.dto.OAuthAttributesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberMapper memberMapper;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        String providerType = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.from(providerType);
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        OAuthAttributesDTO extractAttributes = OAuthAttributesDTO.of(provider, userNameAttributeName, user.getAttributes());

        MemberVO extracted = extractAttributes.getOAuth2UserInfo().toVO();
        Long providerId = extracted.getProviderId();
        // 기존 회원 확인
        MemberVO member = memberMapper.findByProviderAndProviderId(provider, providerId);
        // 회원이 없으면 → 최초 로그인(회원가입)
        if (member == null) {
            memberMapper.insert(extracted);
            AuthVO auth = new AuthVO(extracted.getMemberId(), "ROLE_MEMBER");
            memberMapper.insertAuth(auth);
            member = memberMapper.get(null, extracted.getEmail());
        } else {
            if(updateIfChanged(member, extracted))
                member = memberMapper.get(member.getMemberId(), null);
        }

        return new CustomOAuth2User(user.getAttributes(), userNameAttributeName, member);
    }

    private boolean updateIfChanged(MemberVO origin, MemberVO extracted) {
        boolean needUpdate = false;

        String email = origin.getEmail();
        String name = origin.getName();
        String phone = origin.getPhone();
        Gender gender = origin.getGender();
        LocalDate birth = origin.getBirth();

        if (extracted.getEmail() != null && !extracted.getEmail().equals(email)) {
            email = extracted.getEmail();
            needUpdate = true;
        }
        if (extracted.getName() != null && !extracted.getName().equals(name)) {
            name = extracted.getName();
            needUpdate = true;
        }
        if (extracted.getPhone() != null && !extracted.getPhone().equals(phone)) {
            phone = extracted.getPhone();
            needUpdate = true;
        }
        if (extracted.getGender() != null && !extracted.getGender().equals(gender)) {
            gender = extracted.getGender();
            needUpdate = true;
        }
        if (extracted.getBirth() != null && !extracted.getBirth().equals(birth)) {
            birth = extracted.getBirth();
            needUpdate = true;
        }

        if (needUpdate) {
            log.info("소셜 로그인 사용자 정보 갱신: {}", origin.getEmail());

            MemberVO updated = MemberVO.builder()
                    .memberId(origin.getMemberId())  // 반드시 포함되어야 업데이트됨
                    .email(email)
                    .name(name)
                    .phone(phone)
                    .gender(gender)
                    .birth(birth)
                    .build();
            memberMapper.update(updated);
        }
        return needUpdate;
    }
}
