package com.banklab.category.gemini.controller;

import com.banklab.category.gemini.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @GetMapping("/ask")
    public String askGemini(@RequestParam String prompt) {
        return geminiService.generateText(prompt);
    }

    @PostMapping("/classify")
    public List<String> classifyCategories(@RequestBody Set<String> descriptions) {
        return geminiService.classifyCategories(descriptions);
    }
}