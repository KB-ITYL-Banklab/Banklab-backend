package com.banklab.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(token, "logout", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return exists(token);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 값 저장 (prefix:key 형식, TTL 설정)
    public void set(String key, String value, int timeoutMinutes) {
        redisTemplate.opsForValue().set(key, value, timeoutMinutes, TimeUnit.MINUTES);
    }

    // 값 저장 (prefix:key 형식, TTL 설정)
    public void setBySeconds(String key, String value, int timeOutSeconds) {
        redisTemplate.opsForValue().set(key, value, timeOutSeconds, TimeUnit.SECONDS);
    }




    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 값 검증 (인증번호 비교)
    public boolean verify(String key, String inputValue) {
        String stored = get(key);
        return inputValue != null && inputValue.equals(stored);
    }

    public boolean isVerified(String type) {
        return exists(RedisKeyUtil.verified(type));
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
