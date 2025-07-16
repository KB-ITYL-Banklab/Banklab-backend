package com.banklab.typetest.service;

import com.banklab.typetest.domain.Question;
import com.banklab.typetest.dto.TypeTestResultDTO;

import java.util.List;
import java.util.Map;

public interface TypeTestService {
    List<Question> getAllQuestions();
    TypeTestResultDTO submitAnswers(Map<String, Object> payload);
}
