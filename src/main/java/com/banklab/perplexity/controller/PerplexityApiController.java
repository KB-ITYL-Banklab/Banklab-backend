package com.banklab.perplexity.controller;

import com.banklab.perplexity.dto.PerplexityRequestDto;
import com.banklab.perplexity.dto.PerplexityResponseDto;
import com.banklab.perplexity.service.PerplexityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perplexity")
public class PerplexityApiController {

    private final PerplexityService perplexityService;

    public PerplexityApiController(PerplexityService perplexityService) {
        this.perplexityService = perplexityService;
    }

    @PostMapping("/chat")
    public PerplexityResponseDto chat(@RequestBody PerplexityRequestDto requestDto) {
        return perplexityService.getCompletion(requestDto);
    }
}
