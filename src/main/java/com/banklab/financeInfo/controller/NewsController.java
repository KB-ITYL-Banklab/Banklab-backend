package com.banklab.financeInfo.controller;

import com.banklab.financeInfo.dto.NewsItemDto;
import com.banklab.financeInfo.service.NaverNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NaverNewsService naverNewsService;

    @GetMapping
    public List<NewsItemDto> getNews(@RequestParam(defaultValue = "금융") String keyword) {
        return naverNewsService.searchNews(keyword);
    }
}
