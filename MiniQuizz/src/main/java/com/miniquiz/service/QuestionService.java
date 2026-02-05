package com.miniquiz.service;

import java.util.List;
import java.util.Optional;

import com.miniquiz.model.Question;

public interface QuestionService {

    public List<Question> findAllQuestions();

    public Optional<Question> findQuestionById(Long id);

    public Question saveQuestion(Question newQuestion);

    public String deleteQuestion(Long id);

    public String updateQuestion(Question newQuestion);

    // Preguntas de un bloque
    public List<Question> findQuestionsByBlock(Long blockId);

    // Contar preguntas de un bloque
    public Long countQuestionsByBlock(Long blockId);
    
    public long countByBlockId(Long blockId);
    
    public void deleteAllByBlockId(Long blockId);
}
