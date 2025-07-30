package com.banklab.verification.sender;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationSender implements VerificationSender {
    @Value("${mail.smtp.username}")
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
    public void sendCode(String email) {
        if (redisService.exists(RedisKeyUtil.resend(email))) {
            throw new IllegalStateException("인증번호는 1분 뒤에 재전송 가능합니다.");
        }

        String code = generateCode();
        redisService.set(RedisKeyUtil.email(email), code, 5);
        redisService.set(RedisKeyUtil.resend(email), "true", 1);

        mailSender.send(createMessage(email, code));
    }

    @Override
    public boolean verifyCode(String email, String inputCode) {
        boolean success = redisService.verify(RedisKeyUtil.email(email), inputCode);
        if (success) {
            redisService.set(RedisKeyUtil.verified(email), "true", 10);
            redisService.delete(RedisKeyUtil.email(email));
        }
        return success;
    }
}
