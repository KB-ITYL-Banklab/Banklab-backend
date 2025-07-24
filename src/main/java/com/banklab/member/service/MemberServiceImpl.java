package com.banklab.member.service;

import com.banklab.member.dto.MemberDTO;
import com.banklab.member.dto.MemberJoinDTO;
import com.banklab.member.dto.MemberUpdateDTO;
import com.banklab.member.exception.PasswordMissmatchException;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.security.account.domain.AuthVO;
import com.banklab.security.account.domain.MemberVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper mapper;
    private final PasswordEncoder passwordEncoder;


    // 회원 정보 조회
    @Override
    public MemberDTO get(Long id, String email) {
        MemberVO member = Optional.ofNullable(mapper.get(id, email))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(member);
    }

    // 회원 가입(선언적 트랜잭션 처리)
    @Transactional  // 트랜잭션 처리 보장
    @Override
    public MemberDTO join(MemberVO member) {

        mapper.insert(member);

        // 권한정보 저장
        AuthVO auth = new AuthVO(member.getMemberId(), "ROLE_MEMBER");
        mapper.insertAuth(auth);

        // 저장된 회원정보 반환
        return get(null, member.getEmail());
    }

    @Override
    public boolean checkDuplicate(String email) {
        MemberVO member = mapper.findByEmail(email);
        return member != null;
    }

    @Transactional
    @Override
    public MemberDTO update(Long id, MemberUpdateDTO member) {
        MemberVO vo = mapper.get(id, null);
        // 비밀번호 검증
        if(!passwordEncoder.matches(member.getPassword(), vo.getPassword())) {
            throw new PasswordMissmatchException();
        }
        mapper.update(member.toVO(id));
        return get(id, null);
    }
}
