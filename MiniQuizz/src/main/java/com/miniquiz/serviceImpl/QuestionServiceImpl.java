package com.miniquiz.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miniquiz.model.Question;
import com.miniquiz.repository.QuestionRepo;
import com.miniquiz.service.QuestionService;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionRepo questionRepositorio;

    @Override
    public List<Question> findAllQuestions() {
        return questionRepositorio.findAll();
    }

    @Override
    public Optional<Question> findQuestionById(Long id) {
        return questionRepositorio.findById(id);
    }

    @Override
    public Question saveQuestion(Question newQuestion) {
        if (newQuestion != null) {
            return questionRepositorio.save(newQuestion);
        }
        return new Question();
    }

    @Override
    public String deleteQuestion(Long id) {
        Optional<Question> q = questionRepositorio.findById(id);
        if (q.isPresent()) {
            questionRepositorio.deleteById(id);
            return "Pregunta eliminada satisfactoriamente";
        }
        return "La pregunta no existe";
    }

    @Override
    public String updateQuestion(Question questionActualizar) {
        if (questionActualizar != null && questionActualizar.getId() != null
                && questionRepositorio.findById(questionActualizar.getId()).isPresent()) {

            questionRepositorio.save(questionActualizar);
            return "Pregunta " + questionActualizar.getId() + " actualizada";
        } else {
            if (questionActualizar == null || questionActualizar.getId() == null) {
                return "No se ha podido actualizar (pregunta nula o sin id)";
            }
            return "No se ha podido actualizar " + questionActualizar.getId();
        }
    }

    @Override
    public List<Question> findQuestionsByBlock(Long blockId) {
        return questionRepositorio.findQuestionsByBlockId(blockId);
    }

    @Override
    public Long countQuestionsByBlock(Long blockId) {
        return questionRepositorio.countQuestionsByBlockId(blockId);
    }

    // ✅ NUEVOS (los que necesitas para el borrado controlado)

    @Override
    public long countByBlockId(Long blockId) {
        return questionRepositorio.countByBlockId(blockId);
    }

    @Override
    @Transactional
    public void deleteAllByBlockId(Long blockId) {
        questionRepositorio.deleteByBlockId(blockId);
    }
}
