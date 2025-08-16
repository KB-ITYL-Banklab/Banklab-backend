package com.banklab.category.service;

import com.banklab.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class UtilService {
    private final RedisService redisService;

    public void storeInRedis(String redisKey, String categoryId) {
        redisService.set(redisKey, categoryId, 30);
    }

    public Long isStoredInRedis(String redisKey) {
        String cachedCategory = redisService.get(redisKey);
        if (cachedCategory != null) {
            try {
                return Long.parseLong(cachedCategory);
            } catch (NumberFormatException e) {
                log.warn("캐시된 카테고리 변환 실패: {}", cachedCategory);
            }
        }
        return null;
    }

    private static final Set<String> CAFE_KEYWORDS = Set.of("카페",
            "커피빈", "할리스", "엔제리너스", "폴바셋", "매머드커피", "탐앤탐스", "더벤티", "카페베네", "공차", "메가엠지씨","메가커피","MGC",
            "컴포즈", "아이스크림", "빙수", "브레드", "빵", "생크림", "디저트",
            "cafe", "coffee", "bakery", "dessert",
            "twosome", "hollys", "paulbassett", "angelinus", "starbucks");

    private static final Set<String> HOUSING_KEYWORDS = Set.of(
            "관리사무소","월세", "주거","통신", "전세", "보증금", "렌탈", "인터넷요금", "휴대전화", "통신요금", "전기요금", "가스요금", "수도요금",
            "ktm모바일", "헬로모바일", "sk텔레콤", "skt", "kt", "lg유플러스", "u+", "전력", "수도", "가스", "한전", "한국전력", "임대"
    );

    private static final Set<String> FOOD_KEYWORDS = Set.of("음식점",
            "배달의민족","우아한형제", "요기요", "쿠팡이츠", "배민", "식당", "뷔페", "카레", "브런치", "샐러드", "반찬", "김치", "찌개", "냉면",
            "칼국수", "국밥", "떡볶이", "마라탕", "족발보쌈", "호프", "포차", "술집", "치킨", "족발", "보쌈", "햄버거", "피자",
            "bbq", "bhc", "교촌", "굽네", "맘스터치");

    private static final Set<String> TRANSPORT_KEYWORDS = Set.of(
            "지하철역", "주유소, 충전소", "주차장","철도","코레일",
            "쏘카", "그린카", "렌터카", "렌트카", "주차", "하이카", "주유", "카카오택시", "티머니", "모빌리티",
            "시외버스", "고속버스", "버스터미널", "정류장", "카셰어링", "ev충전", "충전소", "버스", "지하철", "ktx", "srt", "택시"
    );

    private static final Set<String> SHOPPING_KEYWORDS = Set.of("대형마트",
            "마트", "편의점","씨유","지에스","쇼핑", "백화점", "슈퍼", "의류", "화장품", "문구", "잡화", "생활용품",
            "올리브영", "다이소", "유니클로", "이마트", "롯데마트", "홈플러스", "gs25", "cu", "세븐일레븐", "무신사", "abc마트"
    );

    private static final Set<String> LEISURE_KEYWORDS = Set.of("문화시설", "관광명소",
            "영화관", "문화", "예술", "공연", "노래방", "게임", "방탈출", "전시", "레저", "키즈카페",
            "메가박스", "cgv", "롯데시네마", "스크린골프", "볼링", "만화카페", "스포츠", "체육관", "헬스", "짐", "gym"
    );

    private static final Set<String> TRANSFER_KEYWORDS = Set.of(
            "이체", "송금", "정산", "모임회비", "카카오페이", "토스", "페이코", "현금이체", "은행",
            "toss", "payco", "kakaopay", "spay", "네이버페이", "신한페이판", "삼성페이", "pay"
    );


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
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

}
