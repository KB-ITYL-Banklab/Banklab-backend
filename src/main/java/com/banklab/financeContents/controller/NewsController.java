package com.banklab.financeContents.controller;

import com.banklab.financeContents.dto.NewsItemDto;
import com.banklab.financeContents.service.NaverNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api(tags = "경제뉴스 API", description = "네이버 뉴스 API로 실시간 경제뉴스 제공")
public class NewsController {

    // 네이버 뉴스 서비스 의존성 주입 (@Autowired 사용)
    @Autowired
    private NaverNewsService naverNewsService;

    /**
     * 기본 뉴스 조회 API (기본값: "금융" 키워드)
     * @param keyword 검색 키워드 (기본값: "금융")
     * @return 뉴스 목록
     */
    @ApiOperation(value = "경제뉴스 검색", notes = "네이버 뉴스 API를 통해 경제 관련 뉴스를 검색.")
    @GetMapping
    public List<NewsItemDto> getNews(
            @ApiParam(value = "검색할 뉴스 키워드", example = "금융", defaultValue = "금융")
            @RequestParam(defaultValue = "금융") String keyword) {
        // 네이버 뉴스 서비스를 통해 뉴스 검색
        return naverNewsService.searchNews(keyword);
    }
}
