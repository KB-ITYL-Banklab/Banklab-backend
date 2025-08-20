package com.banklab.member.service;

import com.banklab.config.MailConfig;
import com.banklab.config.RedisConfig;
import com.banklab.config.RootConfig;
import com.banklab.member.domain.Gender;
import com.banklab.member.dto.*;
import com.banklab.member.exception.PasswordMissmatchException;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.security.oauth2.domain.OAuth2Provider;
import com.banklab.security.account.domain.MemberVO;
import com.banklab.security.config.SecurityConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class, RedisConfig.class, MailConfig.class})
@Log4j2
@Transactional
class MemberServiceImplTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 존재하는_사용자ID로_회원정보_조회시_정상반환확인() {
        String username = "admin@example.com";

        MemberDTO dto = memberService.get(null, username);

        assertNotNull(dto);
        assertEquals(username, dto.getEmail());
    }

    @Test
    void 존재하지_않는_사용자ID로_회원정보_조회시_예외처리확인() {
        String username = "nonexistent";

        assertThrows(NoSuchElementException.class, () -> {
            memberService.get(null, username);
        });
    }


    @Test
    void 회원가입_성공_테스트() {
        // given
        MemberJoinDTO dto = new MemberJoinDTO();
        dto.setPassword("1234pass!");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setPhone("01012345678");
        dto.setGender(Gender.MALE);
        dto.setBirth("2009-10-16");

        // when
        MemberDTO result = memberService.join(dto);

        // then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Gender.MALE, result.getGender());

        // DB에 실제로 잘 저장되었는지 확인
        MemberVO saved = memberMapper.get(null, "test@example.com");
        assertNotNull(saved);
        assertEquals("ttest@example.com", saved.getEmail());
        assertEquals(Gender.MALE, saved.getGender());
        assertTrue(saved.getPassword().startsWith("$2a$"));
    }

    @Test
    void 비밀번호_수정실패_불일치() {
        // given
        MemberJoinDTO joinDTO = new MemberJoinDTO();
        joinDTO.setName("홍길동");
        joinDTO.setEmail("pwtest@example.com");
        joinDTO.setPhone("010-2222-3333");
        joinDTO.setPassword("Password123!");
        joinDTO.setGender(Gender.MALE);
        joinDTO.setBirth("2009-10-16");
        MemberVO member = joinDTO.toVO(passwordEncoder);

        memberMapper.insert(member);

        // when
        MemberUpdateDTO updateDTO = new MemberUpdateDTO();
        updateDTO.setPassword("wrongPassword"); // 기존 비밀번호 불일치

        assertThrows(PasswordMissmatchException.class,
                () -> memberService.update(member.getMemberId(), updateDTO));
    }

    @Test
    void 존재하지_않는_소셜로그인_정보_조회() {
        // Given
        OAuth2Provider provider = OAuth2Provider.KAKAO;
        Long providerId = 99999L;

        // Then: 결과가 null인지 확인
        MemberVO actualMember = memberMapper.findByProviderAndProviderId(provider, providerId);
        assertNull(actualMember);
    }

    @Test
    void 아이디찾기_성공() {
        MemberJoinDTO joinDTO = new MemberJoinDTO();
        joinDTO.setEmail("findme@example.com");
        joinDTO.setPhone("010-9999-0000");
        joinDTO.setPassword("Password123!");

        MemberDTO member = memberService.join(joinDTO);

        PersonalInfoDTO personal = new PersonalInfoDTO();
        personal.setName(member.getName());
        personal.setBirth(LocalDate.of(2000, 1, 1).toString()); // 가입시 넣은 값과 맞춰야 함
        personal.setPhone("010-9999-0000");

        // 전화번호 인증도 Redis에 미리 세팅되어 있어야 성공
        FindResponseDTO result = memberService.findEmail(personal);

        assertEquals("findme@example.com", result.getEmail());
    }
}