package com.banklab.category.kakaomap.service;

import com.banklab.category.kakaomap.client.KakaoMapClient;
import com.banklab.category.kakaomap.dto.KakaoMapSearchResponseDto;
import com.banklab.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class KakaoMapService {
    private final KakaoMapClient kakaoMapClient;
    private final RedisService redisService;

    private static final long CACHE_TTL_SECONDS = 60 * 60 * 6; // 6시간

    public long getCategoryByDesc(String redisKey, String desc) {
        long categoryId = mapToInternalCategory(desc);

        if(categoryId!=8){
            storeInRedis(redisKey, String.valueOf(categoryId));
            return categoryId;
        }

        // 2. 캐시에 없거나 기타로 분류되면 API 호출
        KakaoMapSearchResponseDto response = kakaoMapClient.getCategoryByDesc(desc);

        String categoryName = null;
        if (response != null && !response.getDocuments().isEmpty()) {
            categoryName = response.getDocuments().get(0).getCategoryName();
        }

        // 넘어온 값이 없는 경우
        if (categoryName == null || categoryName.isEmpty()) {
            categoryName = desc;
        }

        categoryId = mapToInternalCategory(categoryName);
        log.info("상호명: {}, 분류된 카테고리: {}, 카테고리 id: {}", desc, categoryName, categoryId);


        // 3. 결과 캐시에 저장 (TTL 포함)
        storeInRedis(redisKey, String.valueOf(categoryId));

        return categoryId;
    }

    public void storeInRedis(String redisKey, String categoryId) {
        redisService.set(redisKey, categoryId,30);
    }

    public Long isStoredInRedis(String redisKey) {

        String cachedCategory =redisService.get(redisKey);
        if (cachedCategory != null) {
            try {
                return Long.parseLong(cachedCategory);
            } catch (NumberFormatException e) {
                log.warn("캐시된 카테고리 변환 실패: {}", cachedCategory);
            }
        }
        return null;
    }


    private static final Set<String> CAFE_KEYWORDS = Set.of("카페", "커피", "디저트", "제과", "베이커리", "파스쿠찌", "투썸", "이디야", "스타벅스", "빽다방", "던킨", "크로플");
    private static final Set<String> HOUSING_KEYWORDS = Set.of("관리비", "월세", "주거", "통신", "휴대폰", "인터넷", "수도", "가스", "전기", "공과금", "임대", "kt", "skt", "lgu+", "sk브로드밴드");
    private static final Set<String> FOOD_KEYWORDS = Set.of("한식", "중식", "일식", "양식", "분식", "치킨", "패스트푸드", "피자", "족발", "음식점", "김밥", "도시락", "고기", "삼겹살", "한우", "회", "초밥", "햄버거", "맥도날드", "롯데리아", "버거킹", "도미노");
    private static final Set<String> TRANSPORT_KEYWORDS = Set.of("버스", "지하철", "코레일", "택시", "항공", "주유소", "교통", "기름", "ktx", "srt", "t-money", "하이패스", "톨게이트", "오토바이");
    private static final Set<String> SHOPPING_KEYWORDS = Set.of("마트", "편의점", "쇼핑", "백화점", "슈퍼", "의류", "화장품", "문구", "잡화", "생활용품", "올리브영", "다이소", "유니클로", "이마트", "롯데마트", "홈플러스", "gs25", "cu", "세븐일레븐");
    private static final Set<String> LEISURE_KEYWORDS = Set.of("영화관", "문화", "예술", "공연", "노래방", "게임", "방탈출", "전시", "레저", "키즈카페", "메가박스", "cgv", "롯데시네마", "스크린골프", "볼링", "만화카페", "스포츠", "체육관", "헬스");
    private static final Set<String> TRANSFER_KEYWORDS = Set.of("이체", "송금", "정산", "모임회비", "카카오페이", "토스", "페이코", "현금이체", "은행");


    public long mapToInternalCategory(String kakaoCategoryName) {
        kakaoCategoryName = kakaoCategoryName.toLowerCase().trim();

        if (containsAny(kakaoCategoryName, CAFE_KEYWORDS)) return 1;
        if (containsAny(kakaoCategoryName, HOUSING_KEYWORDS)) return 2;
        if (containsAny(kakaoCategoryName, FOOD_KEYWORDS)) return 3;
        if (containsAny(kakaoCategoryName, TRANSPORT_KEYWORDS)) return 4;
        if (containsAny(kakaoCategoryName, SHOPPING_KEYWORDS)) return 5;
        if (containsAny(kakaoCategoryName, LEISURE_KEYWORDS)) return 6;
        if (containsAny(kakaoCategoryName, TRANSFER_KEYWORDS) || containsPersonName(kakaoCategoryName)) return 7;

        return 8; // 기타
    }


    private boolean containsPersonName(String input) {
        return input.matches("^(김|이|박|최|정|조|윤|장|임|문|권|송|)[가-힣]{1,2}$");
    }

    private boolean containsAny(String input, Set<String> keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword.toLowerCase()) || keyword.toLowerCase().contains(input)) {
                return true;
            }
        }
        return false;
    }

}