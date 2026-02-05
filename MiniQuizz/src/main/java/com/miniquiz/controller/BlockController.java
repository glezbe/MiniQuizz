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
import com.miniquiz.model.User;
import com.miniquiz.service.BlockService;
import com.miniquiz.service.QuestionService;
import com.miniquiz.service.RoomService;

@Controller
public class BlockController {

    @Autowired
    private BlockService blockService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private QuestionService questionService;

    /* ============================
     * LISTAR BLOQUES (solo los míos)
     * ============================ */
    @GetMapping("/block")
    public String getBlocks(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Block> losBlocks = blockService.findBlocksByOwner(user);
        model.addAttribute("blocks", losBlocks);

        return "blocks";
    }

    /* ============================
     * VER BLOQUE POR ID (solo si es mío)
     * ============================ */
    @GetMapping("/block/{id}")
    public String getBlockById(@PathVariable Long id, Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Block> blockOpt = blockService.findBlockById(id);

        if (!blockOpt.isPresent()
                || blockOpt.get().getOwner() == null
                || !blockOpt.get().getOwner().getId().equals(user.getId())) {

            model.addAttribute("msg", "No tienes acceso a este bloque");
            model.addAttribute("vacio", true);
            return "block";
        }

        model.addAttribute("block", blockOpt.get());
        model.addAttribute("vacio", false);
        return "block";
    }

    /* ============================
     * FORMULARIO NUEVO BLOQUE
     * ============================ */
    @RequestMapping("/block/add")
    public String addBlock(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("block", new Block());
        model.addAttribute("nuevo", true);
        return "blockForm";
    }

    /* ============================
     * FORMULARIO EDITAR BLOQUE (solo si es mío)
     * ============================ */
    @RequestMapping("/block/update/{id}")
    public String updateBlock(@PathVariable Long id, Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Block> blockOpt = blockService.findBlockById(id);

        if (!blockOpt.isPresent()
                || blockOpt.get().getOwner() == null
                || !blockOpt.get().getOwner().getId().equals(user.getId())) {

            return "redirect:/block";
        }

        model.addAttribute("block", blockOpt.get());
        model.addAttribute("nuevo", false);
        return "blockForm";
    }

    /* ============================
     * BORRAR BLOQUE (solo si es mío) + REGLAS
     * ============================ */
    @GetMapping("/block/delete/{id}")
    public String deleteBlock(@PathVariable Long id, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Block> blockOpt = blockService.findBlockById(id);

        // si no existe o no es suyo
        if (!blockOpt.isPresent()
                || blockOpt.get().getOwner() == null
                || !blockOpt.get().getOwner().getId().equals(user.getId())) {
            return "redirect:/block";
        }

        // ✅ Regla 3: si el bloque está usado en alguna sala -> prohibido
        if (roomService.existsByBlockId(id)) {
            model.addAttribute("title", "No se puede eliminar el bloque");
            model.addAttribute("msg", "Este bloque está asociado a una sala (WAITING/RUNNING/FINISHED).");
            model.addAttribute("blockId", id);
            model.addAttribute("canDeleteQuestions", false);
            return "blockDeleteWarning";
        }

        // ✅ Regla 2: si no está usado pero tiene preguntas -> no permitir y mostrar botón borrar preguntas
        long qCount = questionService.countByBlockId(id);
        if (qCount > 0) {
            model.addAttribute("title", "No se puede eliminar el bloque");
            model.addAttribute("msg", "Hay preguntas en este bloque (" + qCount + "). Primero elimínalas.");
            model.addAttribute("blockId", id);
            model.addAttribute("canDeleteQuestions", true);
            return "blockDeleteWarning";
        }

        // ✅ Regla 1: no usado + sin preguntas -> borrar
        blockService.deleteBlock(id);
        return "redirect:/block";
    }

    /* ============================
     * BORRAR TODAS LAS PREGUNTAS DE UN BLOQUE (solo si es mío)
     * ============================ */
    @PostMapping("/block/{id}/questions/delete-all")
    public String deleteAllQuestionsOfBlock(@PathVariable Long id, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Block> blockOpt = blockService.findBlockById(id);

        // si no existe o no es suyo
        if (!blockOpt.isPresent()
                || blockOpt.get().getOwner() == null
                || !blockOpt.get().getOwner().getId().equals(user.getId())) {
            return "redirect:/block";
        }

        // Extra seguridad: si está asociado a salas, no dejamos tocar
        if (roomService.existsByBlockId(id)) {
            model.addAttribute("title", "No se pueden borrar preguntas");
            model.addAttribute("msg", "Este bloque está asociado a una sala. No se pueden borrar sus preguntas.");
            model.addAttribute("blockId", id);
            model.addAttribute("canDeleteQuestions", false);
            return "blockDeleteWarning";
        }

        questionService.deleteAllByBlockId(id);
        return "redirect:/block/update/" + id; // o /block/{id}
    }

    /* ============================
     * GUARDAR BLOQUE (nuevo o editado)
     * ============================ */
    @PostMapping("/block/save")
    public String saveBlock(
            @Valid @ModelAttribute("block") Block block,
            BindingResult result,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("nuevo", block.getId() == null);
            return "blockForm";
        }

        // Si es edición, comprobamos que sea del usuario
        if (block.getId() != null) {
            Optional<Block> existing = blockService.findBlockById(block.getId());
            if (!existing.isPresent()
                    || existing.get().getOwner() == null
                    || !existing.get().getOwner().getId().equals(user.getId())) {

                return "redirect:/block";
            }
        }

        block.setOwner(user);
        blockService.saveBlock(block);

        return "redirect:/block";
    }
}
