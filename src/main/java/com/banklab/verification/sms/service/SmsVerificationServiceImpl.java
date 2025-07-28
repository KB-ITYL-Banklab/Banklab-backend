package com.banklab.verification.sms.service;

import com.banklab.common.redis.RedisKeyUtil;
import com.banklab.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsVerificationServiceImpl implements SmsVerificationService {

    @Value("${coolsms.api-key}")
    private String SMS_API_KEY;

    @Value("${coolsms.secret-key}")
    private String SMS_API_SECRET;

    @Value("${coolsms.fromnumber}") // 발신자 번호 주입
    private String FROM_NUMBER;

    private final RedisService redisService;

    // 인증번호 생성 (인증번호 6자리)
    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    private Message createMessage(String to, String code) {
        Message message = new Message();

        message.setFrom(FROM_NUMBER);
        message.setTo(to);
        message.setText("[BankLab] 인증번호 [" + code + "]를 입력해주세요.");

        return message;
    }

    // 인증번호 전송하기
    @Transactional
    @Override
    public void sendVerificationCode(String phone) {
        // 재전송 제한 체크 (DoS 공격 방지)
        if (redisService.exists(RedisKeyUtil.resend(phone))) {
            throw new IllegalStateException("인증번호는 1분 뒤에 재전송 가능합니다.");
        }
        // 랜덤 인증번호 생성
        String code = generateCode();

        // 인증번호 redis에 저장 (만료시간: 3분)
        redisService.set(RedisKeyUtil.sms(phone), code, 3);

        // 재전송 제한 설정 (1분)
        redisService.set(RedisKeyUtil.resend(phone), "true", 1);

        Message message = createMessage(phone, code);
        DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(SMS_API_KEY, SMS_API_SECRET, "https://api.solapi.com");
        try {
            messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception exception) {
            log.info(exception.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String phone, String inputCode) {
        boolean success = redisService.verify(RedisKeyUtil.sms(phone), inputCode);
        if (success) {
            redisService.set(RedisKeyUtil.verified(phone), "true", 10);
            redisService.delete(RedisKeyUtil.sms(phone)); // 인증번호 삭제
        }
        return success;
    }
}
