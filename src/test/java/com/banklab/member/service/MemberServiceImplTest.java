package com.banklab.member.service;

import com.banklab.config.RootConfig;
import com.banklab.member.dto.MemberDTO;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.security.account.domain.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@Log4j2
class MemberServiceImplTest {

    @Autowired
    private MemberService memberService;

    @Test
    void 존재하는_사용자ID로_회원정보_조회시_정상반환확인() {
        String username = "admin";

        MemberDTO dto = memberService.get(username);

        assertNotNull(dto);
        assertEquals(username, dto.getUsername());
    }

    @Test
    void 존재하지_않는_사용자ID로_회원정보_조회시_예외처리확인() {
        String username = "nonexistent";

        assertThrows(NoSuchElementException.class, () -> {
            memberService.get(username);
        });
    }
}