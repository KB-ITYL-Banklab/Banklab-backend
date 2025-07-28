package com.banklab.verification.email.service;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import com.banklab.verification.email.dto.EmailVerifyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    @Value("mail.smtp.username")
    private String fromEmail;

    private final JavaMailSender mailSender;

    private final RedisService redisService;

    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    private SimpleMailMessage createMessage(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(fromEmail);
        message.setSubject("[BankLab] 이메일 인증번호");
        message.setText(String.format("요청하신 화면에 아래 인증번호를 5분 내에 입력해주세요.\n뱅크랩 인증번호는 [%s]입니다.", code));
        return message;
    }

    @Override
    public void sendVerificationCode(String email) {
        // 재전송 제한 체크 (DoS 공격 방지)
        if (redisService.exists(RedisKeyUtil.resend(email))) {
            throw new IllegalStateException("인증번호는 1분 뒤에 재전송 가능합니다.");
        }
        // 랜던 인증번호 생성 (6자리)
        String code = generateCode();

        // 인증번호 redis에 저장 (만료시간: 5분)
        redisService.set(RedisKeyUtil.email(email), code, 5);

        // 재전송 제한 설정 (1분)
        redisService.set(RedisKeyUtil.resend(email), "true", 1);

        SimpleMailMessage message = createMessage(email, code);
        mailSender.send(message);
    }

    @Override
    public boolean verifyCode(EmailVerifyDTO dto) {
        String email = dto.getEmail();
        String inputCode = dto.getCode();
        boolean success = redisService.verify(RedisKeyUtil.email(email), inputCode);
        if (success) {
            redisService.set(RedisKeyUtil.verified(email), "true", 10);
            redisService.delete(RedisKeyUtil.email(email)); // 인증번호 삭제
        }
        return success;
    }
}
