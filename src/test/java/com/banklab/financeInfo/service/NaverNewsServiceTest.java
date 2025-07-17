package com.banklab.financeInfo.service;

import com.banklab.config.RootConfig;
import com.banklab.financeInfo.dto.NewsItemDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class})
class NaverNewsServiceTest {

    @Autowired
    private NaverNewsService naverNewsService;

    @Test
    void searchNews() {
        // given
        String query = "금융";

        // when
        List<NewsItemDto> newsItems = naverNewsService.searchNews(query);

        // then
        assertNotNull(newsItems);
        assertFalse(newsItems.isEmpty());
        newsItems.forEach(item -> {
            assertNotNull(item.getTitle());
            assertNotNull(item.getLink());
            assertNotNull(item.getDescription());
            assertNotNull(item.getPubDate());
        });

        for(NewsItemDto dto : newsItems){
            System.out.println(dto.getTitle());
            System.out.println(dto.getOriginallink());
            System.out.println(dto.getDescription());
            System.out.println(dto.getLink());
            System.out.println(dto.getPubDate());
        }
    }
}
