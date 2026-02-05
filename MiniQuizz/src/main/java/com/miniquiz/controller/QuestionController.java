package com.miniquiz.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.miniquiz.model.Block;
import com.miniquiz.model.Question;
import com.miniquiz.model.User;
import com.miniquiz.service.BlockService;
import com.miniquiz.service.QuestionService;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private BlockService blockService;

    // =========================
    // Helpers
    // =========================
    private User getLoggedUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    private boolean isOwner(User user, Block block) {
        return user != null
                && block != null
                && block.getOwner() != null
                && block.getOwner().getId().equals(user.getId());
    }

    // =========================
    // LISTA: preguntas de un bloque (solo si el bloque es mío)
    // =========================
    @GetMapping("/question/block/{id}")
    public String getQuestionsByBlock(@PathVariable Long id, Model model, HttpSession session) {

        User user = getLoggedUser(session);
        if (user == null) return "redirect:/login";

        Optional<Block> blockOpt = blockService.findBlockById(id);
        if (!blockOpt.isPresent() || !isOwner(user, blockOpt.get())) {
            return "redirect:/block";
        }

        Block block = blockOpt.get();
        List<Question> preguntas = questionService.findQuestionsByBlock(id);

        model.addAttribute("block", block);
        model.addAttribute("questions", preguntas);
        model.addAttribute("numPreguntas", preguntas.size());

        Long count = questionService.countQuestionsByBlock(id);
        model.addAttribute("usable", count != null && count >= 20);

        return "questions";
    }

    // =========================
    // VER pregunta (solo si pertenece a uno de mis bloques)
    // =========================
    @GetMapping("/question/{id}")
    public String getQuestionById(@PathVariable Long id, Model model, HttpSession session) {

        User user = getLoggedUser(session);
        if (user == null) return "redirect:/login";

        Optional<Question> qOpt = questionService.findQuestionById(id);
        if (!qOpt.isPresent()) {
            model.addAttribute("msg", "No existe la pregunta con id " + id);
            model.addAttribute("vacio", true);
            return "question";
        }

        Question q = qOpt.get();
        if (q.getBlock() == null || !isOwner(user, q.getBlock())) {
            return "redirect:/block";
        }

        model.addAttribute("question", q);
        model.addAttribute("vacio", false);
        return "question";
    }

    // =========================
    // FORM: añadir pregunta a bloque (solo si el bloque es mío)
    // =========================
    @RequestMapping("/question/add/block/{id}")
    public String addQuestion(@PathVariable Long id, Model model, HttpSession session) {

        User user = getLoggedUser(session);
        if (user == null) return "redirect:/login";

        Optional<Block> blockOpt = blockService.findBlockById(id);
        if (!blockOpt.isPresent() || !isOwner(user, blockOpt.get())) {
            return "redirect:/block";
        }

        Block block = blockOpt.get();
        Question q = new Question();
        q.setBlock(block);

        model.addAttribute("question", q);
        model.addAttribute("block", block);
        model.addAttribute("nuevo", true);

        return "questionForm";
    }

    // =========================
    // FORM: editar pregunta (solo si pertenece a uno de mis bloques)
    // =========================
    @RequestMapping("/question/update/{id}")
    public String updateQuestion(@PathVariable Long id, Model model, HttpSession session) {

        User user = getLoggedUser(session);
        if (user == null) return "redirect:/login";

        Optional<Question> qOpt = questionService.findQuestionById(id);
        if (!qOpt.isPresent()) return "redirect:/block";

        Question q = qOpt.get();
        if (q.getBlock() == null || !isOwner(user, q.getBlock())) {
            return "redirect:/block";
        }

        model.addAttribute("question", q);
        model.addAttribute("block", q.getBlock());
        model.addAttribute("nuevo", false);

        return "questionForm";
    }

    // =========================
    // BORRAR pregunta (solo si pertenece a uno de mis bloques)
    // =========================
    @GetMapping("/question/delete/{id}")
    public String deleteQuestion(@PathVariable Long id, HttpSession session) {

        User user = getLoggedUser(session);
        if (user == null) return "redirect:/login";

        Optional<Question> qOpt = questionService.findQuestionById(id);
        if (!qOpt.isPresent()) return "redirect:/block";

        Question q = qOpt.get();
        if (q.getBlock() == null || !isOwner(user, q.getBlock())) {
            return "redirect:/block";
        }

        Long blockId = q.getBlock().getId();
        questionService.deleteQuestion(id);

        return "redirect:/question/block/" + blockId;
    }

    // =========================
    // GUARDAR pregunta (nuevo o editar) + control por bloque owner
    // =========================
    @PostMapping("/question/save")
    public String saveQuestion(
            @Valid @ModelAttribute("question") Question question,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {

        User user = getLoggedUser(session);
        if (user == null) return "redirect:/login";

        // Validar que haya bloque
        if (question.getBlock() == null || question.getBlock().getId() == null) {
            model.addAttribute("msg", "La pregunta debe pertenecer a un bloque");
            model.addAttribute("nuevo", (question.getId() == null));
            return "questionForm";
        }

        // Cargar bloque real y comprobar owner
        Optional<Block> blockOpt = blockService.findBlockById(question.getBlock().getId());
        if (!blockOpt.isPresent() || !isOwner(user, blockOpt.get())) {
            return "redirect:/block";
        }

        Block block = blockOpt.get();
        question.setBlock(block);

        if (bindingResult.hasErrors()) {
            model.addAttribute("block", block);
            model.addAttribute("nuevo", (question.getId() == null));
            return "questionForm";
        }

        // Si es edición, comprobar que la pregunta original es del usuario
        if (question.getId() != null) {
            Optional<Question> existing = questionService.findQuestionById(question.getId());
            if (!existing.isPresent()
                    || existing.get().getBlock() == null
                    || !isOwner(user, existing.get().getBlock())) {
                return "redirect:/block";
            }
        }

        questionService.saveQuestion(question);
        return "redirect:/question/block/" + block.getId();
    }
}
