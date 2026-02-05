package com.miniquiz.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miniquiz.dto.RoomDraft;
import com.miniquiz.model.Question;
import com.miniquiz.model.User;
import com.miniquiz.service.QuestionService;

@Controller
public class RoomQuestionController {

    @Autowired
    private QuestionService questionService;

    // GET: seleccionar preguntas (del borrador)
    @GetMapping("/room/draft/questions")
    public String selectQuestions(HttpSession session, Model model, RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        RoomDraft draft = (RoomDraft) session.getAttribute("roomDraft");
        if (draft == null || draft.getBlockId() == null) {
            ra.addFlashAttribute("error", "Primero elige un bloque.");
            return "redirect:/room/add";
        }

        List<Question> questions = questionService.findQuestionsByBlock(draft.getBlockId());

        model.addAttribute("questions", questions);
        model.addAttribute("selectedIds", draft.getQuestionIds());

        return "roomQuestionsSelectDraft";
    }

    // POST: guardar selección manual -> pasa a tiempo
    @PostMapping("/room/draft/questions/save-manual")
    public String saveManual(@RequestParam(name="questionIds", required=false) List<Long> questionIds,
                             HttpSession session,
                             RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        RoomDraft draft = (RoomDraft) session.getAttribute("roomDraft");
        if (draft == null || draft.getBlockId() == null) return "redirect:/room/add";

        if (questionIds == null || questionIds.isEmpty()) {
            ra.addFlashAttribute("error", "Selecciona al menos una pregunta.");
            return "redirect:/room/draft/questions";
        }

        draft.setQuestionIds(questionIds);
        session.setAttribute("roomDraft", draft);

        return "redirect:/room/draft/time";
    }

    // POST: guardar aleatorias -> pasa a tiempo
    @PostMapping("/room/draft/questions/save-random")
    public String saveRandom(@RequestParam("count") int count,
                             HttpSession session,
                             RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        RoomDraft draft = (RoomDraft) session.getAttribute("roomDraft");
        if (draft == null || draft.getBlockId() == null) return "redirect:/room/add";

        List<Question> all = questionService.findQuestionsByBlock(draft.getBlockId());
        if (all.isEmpty()) {
            ra.addFlashAttribute("error", "Este bloque no tiene preguntas.");
            return "redirect:/room/draft/questions";
        }

        if (count < 1) count = 1;
        if (count > all.size()) count = all.size();

        Collections.shuffle(all);

        List<Long> selected = new ArrayList<>();
        for (int i = 0; i < count; i++) selected.add(all.get(i).getId());

        draft.setQuestionIds(selected);
        session.setAttribute("roomDraft", draft);

        return "redirect:/room/draft/time";
    }
}
