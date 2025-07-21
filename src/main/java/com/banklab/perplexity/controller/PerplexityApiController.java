package com.banklab.perplexity.controller;

import com.banklab.perplexity.dto.PerplexityRequestDto;
import com.banklab.perplexity.dto.PerplexityResponseDto;
import com.banklab.perplexity.service.PerplexityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perplexity")
@RequiredArgsConstructor
public class PerplexityApiController {

    private final PerplexityService perplexityService;

    @PostMapping("/chat")
    public PerplexityResponseDto chat(@RequestBody PerplexityRequestDto requestDto) {
        return perplexityService.getCompletion(requestDto);
    }
}
