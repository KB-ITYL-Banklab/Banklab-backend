package com.banklab.category.perplexity.controller;

import com.banklab.category.perplexity.service.PerplexityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/perplexity")
@RequiredArgsConstructor
public class PerplexityApiController {

    private final PerplexityService perplexityService;

    @PostMapping("/chat")
    public List<String> chat(@RequestBody List<String> requestDto) {
        return perplexityService.getCompletions(requestDto);
    }
}
