package com.banklab.member.mapper;

import com.banklab.security.oauth2.domain.OAuthProvider;
import com.banklab.security.account.domain.AuthVO;
import com.banklab.security.account.domain.MemberVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface MemberMapper {
    MemberVO get(@Param("id") Long id, @Param("email") String email);
    int insert(MemberVO member);                      // 회원정보 저장
    int insertAuth(AuthVO auth);                      // 권한정보 저장
    List<Long> findAllMemberIds();
    int update(MemberVO member);
    int updatePassword(@Param("email") String email, @Param("password") String password);
    MemberVO findByEmail(String email);
    MemberVO findByProviderAndProviderId(@Param("provider")OAuthProvider provider, @Param("providerId")Long providerId);
    MemberVO findByPhone(String phone);
    MemberVO findByPersonalInfo(@Param("name") String name, @Param("birth") LocalDate birth, @Param("phone") String phone);
}
