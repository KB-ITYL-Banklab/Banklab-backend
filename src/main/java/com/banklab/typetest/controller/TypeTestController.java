package com.banklab.typetest.controller;

import com.banklab.typetest.domain.Question;
import com.banklab.typetest.service.TypeTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banklab.typetest.dto.TypeTestResultDTO;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typetest")
@RequiredArgsConstructor
public class TypeTestController {

    private final TypeTestService typeTestService;

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getAllQuestions() {
        return ResponseEntity.ok(typeTestService.getAllQuestions());
    }

    @PostMapping
    public ResponseEntity<TypeTestResultDTO> submitAnswers(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(typeTestService.submitAnswers(payload));
    }
    @GetMapping("/result/{userId}")
    public ResponseEntity<TypeTestResultDTO> getTestResult(@PathVariable Long userId) {
        return ResponseEntity.ok(typeTestService.getTestResultByUserId(userId));
    }

}
