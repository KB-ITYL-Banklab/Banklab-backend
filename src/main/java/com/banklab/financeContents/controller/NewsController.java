package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.NewsItemDto;
import com.banklab.financeContents.service.NaverNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 네이버 뉴스 API를 활용한 경제뉴스 검색 컨트롤러
 * OPENAPI 연결 흐름: Client -> Controller -> Service -> Naver API
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    // 네이버 뉴스 서비스 의존성 주입 (@Autowired 사용)
    @Autowired
    private NaverNewsService naverNewsService;

    /**
     * 기본 뉴스 조회 API (기본값: "금융" 키워드)
     * @param keyword 검색 키워드 (기본값: "금융")
     * @return 뉴스 목록
     */
    @GetMapping
    public List<NewsItemDto> getNews(@RequestParam(defaultValue = "금융") String keyword) {
        // 네이버 뉴스 서비스를 통해 뉴스 검색
        return naverNewsService.searchNews(keyword);
    }
}
