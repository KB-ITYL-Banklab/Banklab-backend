package com.banklab.member.service;

import com.banklab.member.dto.*;
import com.banklab.security.account.domain.MemberVO;

public interface MemberService {
    /**
     * id 또는 이메일로 회원 정보 조회
     * @param id
     * @param email
     * @return 회원 dto
     */
    MemberDTO get(Long id, String email);    // 두 파라미터 중 하나는 null이어도 됨

    /**
     * 회원가입
     * @param member
     * @return 회원 dto
     */
    MemberDTO join(MemberJoinDTO member);

    /**
     * 회원등록
     * @param member
     * @return 회원 dto
     */
    MemberDTO registerMember(MemberVO member);

    /**
     * 이메일 존재 여부 (이메일 중복 체크)
     * @param email
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 회원 정보 업데이트
     * @param id
     * @param member
     * @return 회원 dto
     */
    MemberDTO update(Long id, MemberUpdateDTO member);

    /**
     * 비밀번호 재설정
     * @param dto
     */
    void resetPassword(PasswordResetDTO dto);

    /**
     * 개인 정보 기반 아이디 찾기
     * @param dto
     * @return 회원 아이디 dto
     */
    FindResponseDTO findEmail(PersonalInfoDTO dto);

    /**
     * 회원 전화번호 조회
     * @param email
     * @return 회원 전화번호
     */
    String getPhoneByEmail(String email);
}
