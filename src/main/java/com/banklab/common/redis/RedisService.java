package com.banklab.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
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

    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
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


    /**
     *  거래 내역 분석 시 사용할 Hash 관련 메서드
     */
    // 개별 상호명에 대한 분류 결과 저장
    public void hset(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    // 특정 상호명에 대한 분류 결과 가져오기
    public String hget(String key, String field) {
        return (String) redisTemplate.opsForHash().get(key, field);
    }

    // 해당 Hash key에 저장된 모든 (상호명, categoryId) 가져오기
    public Map<Object, Object> hgetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    // 해당 Hash에 저장된 상호명 개수
    public Long hlen(String key) {
        return redisTemplate.opsForHash().size(key);
    }
}
