package com.banklab.member.service;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.member.dto.*;
import com.banklab.member.exception.PasswordMissmatchException;
import com.banklab.member.mapper.MemberMapper;
import com.banklab.oauth.domain.OAuthProvider;
import com.banklab.security.account.domain.AuthVO;
import com.banklab.security.account.domain.MemberVO;
import com.banklab.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;


    // 회원 정보 조회
    @Override
    public MemberDTO get(Long id, String email) {
        MemberVO member = Optional.ofNullable(mapper.get(id, email))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(member);
    }

    // 회원 가입(선언적 트랜잭션 처리)
    @Override
    public MemberDTO join(MemberJoinDTO dto) {
        String email = dto.getEmail();
        String phoneNum = dto.getPhone().replace("-", "");
        // 이메일 & 전화번호 인증되었는지 확인
        validateVerification(email, phoneNum);
        if (existsByPhone(phoneNum)) {
            throw new IllegalStateException("이미 가입된 전화번호입니다.");
            //이미 가입된 계정이 있습니다. 로그인 화면으로 이동합니다.
        }
        MemberDTO member = registerMember(dto.toVO(passwordEncoder));

        // 인증상태 삭제 (가입 성공 여부와 상관없이 1회용 인증)
        redisService.delete(RedisKeyUtil.verified(phoneNum));
        redisService.delete(RedisKeyUtil.verified(email));

        // 저장된 회원정보 반환
        return member;
    }

    // 회원 등록
    @Transactional // 트랜잭션 처리 보장
    @Override
    public MemberDTO registerMember(MemberVO member) {
        mapper.insert(member);

        // 권한정보 저장
        AuthVO auth = new AuthVO(member.getMemberId(), "ROLE_MEMBER");
        mapper.insertAuth(auth);

        // 저장된 회원정보 반환
        return get(null, member.getEmail());
    }

    // 이메일 & 전화번호 인증되었는지 확인
    private void validateVerification(String email, String phone) {
        if (!redisService.isVerified(email)) {
            throw new IllegalStateException("이메일 인증을 먼저 완료하세요.");
        }
        if (!redisService.isVerified(phone)) {
            throw new IllegalStateException("전화번호 인증을 먼저 완료하세요.");
        }
    }

    // 전화번호 존재 여부
    private boolean existsByPhone(String phone) {
        MemberVO member = mapper.findByPhone(phone);
        return member != null;
    }

    // 이메일(아이디) 중복 여부
    @Override
    public boolean existsByEmail(String email) {
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

    @Transactional
    @Override
    public void resetPassword(PasswordResetDTO dto) {
        // 회원 조회
        MemberVO vo = mapper.get(null, dto.getEmail());

        // 소셜 로그인 계정 여부
        if (!OAuthProvider.LOCAL.equals(vo.getProvider())) {
            throw new IllegalStateException("소셜 로그인 계정은 비밀번호 변경을 할 수 없습니다.");
        }

        // 인증 여부 확인
        String verifiedKey = dto.getEmailVerified() ? vo.getEmail() : vo.getPhone();
        if (!redisService.isVerified(verifiedKey)) {
            throw new IllegalStateException("인증을 먼저 완료하세요.");
        }

        // 비밀번호 변경
        String encoded = passwordEncoder.encode(dto.getNewPassword());
        mapper.updatePassword(vo.getEmail(), encoded);

        redisService.delete(RedisKeyUtil.verified(verifiedKey));
    }

    // 아이디 찾기
    @Override
    public FindResponseDTO findEmail(PersonalInfoDTO dto) {
        String phoneNum = dto.getPhone().replace("-", "");
        MemberVO member = mapper.findByPersonalInfo(dto.getName(), LocalDate.parse(dto.getBirth()), phoneNum);
        if (!redisService.isVerified(phoneNum)) {
            throw new IllegalStateException("전화번호 인증을 먼저 완료하세요.");
        }
        if (member == null) {
            throw new IllegalStateException("입력하신 정보와 일치하는 계정이 없습니다.");
        }

        redisService.delete(RedisKeyUtil.verified(phoneNum));

        return FindResponseDTO.of(member);
    }
}
