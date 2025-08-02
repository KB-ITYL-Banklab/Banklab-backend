package com.banklab.member.service;

import com.banklab.config.RedisConfig;
import com.banklab.config.RootConfig;
import com.banklab.member.dto.MemberDTO;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.security.oauth2.domain.OAuthProvider;
import com.banklab.security.account.domain.MemberVO;
import com.banklab.security.config.SecurityConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class, RedisConfig.class})
@Log4j2
@Transactional
class MemberServiceImplTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMapper memberMapper;

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

    /*
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
        MemberDTO result = memberService.join(dto.toVO());

        // then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals(Gender.MALE, result.getGender());

        // DB에 실제로 잘 저장되었는지 확인
        MemberVO saved = memberMapper.get("test@example.com");
        assertNotNull(saved);
        assertEquals("ttest@example.com", saved.getEmail());
        assertEquals(Gender.MALE, saved.getGender());
        assertTrue(saved.getPassword().startsWith("$2a$"));
    }
    */

    @Test
    void testFindByProviderAndProviderId_NotFound() {
        // Given
        OAuthProvider provider = OAuthProvider.KAKAO;
        Long providerId = 99999L;

        // Then: 결과가 null인지 확인
        MemberVO actualMember = memberMapper.findByProviderAndProviderId(provider, providerId);
        assertNull(actualMember);
    }
}