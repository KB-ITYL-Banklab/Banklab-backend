package com.banklab.verification.sender;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsVerificationSender implements VerificationSender {
    @Value("${coolsms.api-key}")
    private String SMS_API_KEY;

    @Value("${coolsms.secret-key}")
    private String SMS_API_SECRET;

    @Value("${coolsms.fromnumber}")
    private String FROM_NUMBER;

    private final RedisService redisService;

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

    @Override
    public void sendCode(String phone) {
        if (redisService.exists(RedisKeyUtil.resend(phone))) {
            throw new IllegalStateException("인증번호는 1분 뒤에 재전송 가능합니다.");
        }

        String code = generateCode();
        redisService.set(RedisKeyUtil.sms(phone), code, 3);
        redisService.set(RedisKeyUtil.resend(phone), "true", 1);

        try {
            DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(
                    SMS_API_KEY, SMS_API_SECRET, "https://api.solapi.com"
            );
            messageService.sendOne(new SingleMessageSendingRequest(createMessage(phone, code)));
        } catch (Exception e) {
            log.warn("[SMS 전송 실패] {}", e.getMessage());
        }
    }

    @Override
    public boolean verifyCode(String phone, String inputCode) {
        boolean success = redisService.verify(RedisKeyUtil.sms(phone), inputCode);
        if (success) {
            redisService.set(RedisKeyUtil.verified(phone), "true", 10);
            redisService.delete(RedisKeyUtil.sms(phone));
        }
        return success;
    }
}
