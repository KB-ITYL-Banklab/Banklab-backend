package com.banklab.financeInfo.controller;

import com.banklab.financeInfo.dto.NewsItemDto;
import com.banklab.financeInfo.service.NaverNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NaverNewsController {

    private final NaverNewsService newsService;

    @GetMapping("/search")
    public List<NewsItemDto> searchNews(@RequestParam String query) {
        return newsService.searchNews(query);
    }
}
