package com.banklab.category.kakaomap.service;

import com.banklab.category.kakaomap.client.KakaoMapClient;
import com.banklab.category.kakaomap.dto.KakaoMapSearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class KakaoMapService {
    private final KakaoMapClient kakaoMapClient;
    private final RedisTemplate<String,String> redisTemplate;

    private static final long CACHE_TTL_SECONDS = 60 * 60 * 6; // 6시간

    public long getCategoryByDesc(String redisKey, String desc) {
        long categoryId=8;
        // 2. 캐시 없으면 API 호출
        KakaoMapSearchResponseDto response = kakaoMapClient.getCategoryByDesc(desc);

        if (response != null && !response.getDocuments().isEmpty()) {
            String categoryName = response.getDocuments().get(0).getCategoryName();
            categoryId = mapToInternalCategory(categoryName);
            log.info("상호명: {}, 분류된 카테고리: {}, 카테고리 id: {}", desc, categoryName, categoryId);
        }

        // 3. 결과 캐시에 저장 (TTL 포함)
        storeInRedis(redisKey, String.valueOf(categoryId));

        return categoryId;
    }

    public void storeInRedis(String redisKey, String categoryId){
        redisTemplate.opsForValue().set(
                redisKey,
                categoryId,
                CACHE_TTL_SECONDS,
                TimeUnit.SECONDS );
    }

    public Long isStoredInRedis(String redisKey){
        String cachedCategory = redisTemplate.opsForValue().get(redisKey);
        if (cachedCategory != null) {
            try {
                return Long.parseLong(cachedCategory);
            } catch (NumberFormatException e) {
                log.warn("캐시된 카테고리 변환 실패: {}", cachedCategory);
            }
        }
        return null;
    }

    public long mapToInternalCategory(String kakaoCategoryName) {
        kakaoCategoryName = kakaoCategoryName.toLowerCase();
        kakaoCategoryName = kakaoCategoryName.trim();

        if (containsAny(kakaoCategoryName, "카페", "커피", "디저트", "제과", "베이커리"))
            return 1; // 카페/간식

        if (containsAny(kakaoCategoryName, "관리비", "주거" ,"통신", "휴대폰", "인터넷", "가스", "전기", "수도", "공과금", "임대료"))
            return 2; // 주거/통신

        if (containsAny(kakaoCategoryName, "한식", "중식", "일식", "양식", "분식", "치킨", "패스트푸드", "피자", "족발", "음식점")) {
            return 3; // 식비
        }

        if (containsAny(kakaoCategoryName, "버스", "지하철", "기차", "택시", "항공", "주유소", "교통")) {
            return 4; // 교통
        }

        if (containsAny(kakaoCategoryName, "마트", "편의점", "쇼핑", "백화점", "슈퍼", "의류", "화장품", "문구", "잡화", "생활용품")) {
            return 5; // 쇼핑
        }

        if (containsAny(kakaoCategoryName, "영화관","문화", "예술","공연", "노래방", "게임", "방탈출", "전시", "레저", "키즈카페")) {
            return 6; // 문화/여가
        }

        if (containsAny(kakaoCategoryName, "이체", "송금", "정산", "모임회비"))
            return 7; // 이체

        return 8; // 기타
    }

    private boolean containsAny(String input, String... keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword) || keyword.contains(input)) return true;
        }
        return false;
    }

}