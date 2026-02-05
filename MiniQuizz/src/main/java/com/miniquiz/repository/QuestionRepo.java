package com.miniquiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.miniquiz.model.Question;

public interface QuestionRepo extends JpaRepository<Question, Long> {

    // Traer preguntas de un bloque (para "preguntas dentro de un bloque")
    @Query("SELECT q FROM Question q WHERE q.block.id = ?1 ORDER BY q.id ASC")
    List<Question> findQuestionsByBlockId(Long blockId);

    // Contar preguntas de un bloque (para comprobar >= 20)
    @Query("SELECT COUNT(q) FROM Question q WHERE q.block.id = ?1")
    Long countQuestionsByBlockId(Long blockId);

    // Buscar pregunta por id (igual que el profe)
    @Query("SELECT q FROM Question q WHERE q.id = ?1")
    Optional<Question> findQuestionById(Long id);
    
    long countByBlockId(Long blockId);
    void deleteByBlockId(Long blockId);
}
