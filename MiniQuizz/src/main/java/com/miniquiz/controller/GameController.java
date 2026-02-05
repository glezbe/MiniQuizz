package com.miniquiz.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.miniquiz.concurrency.GameManager;
import com.miniquiz.concurrency.GameState;
import com.miniquiz.model.Player;
import com.miniquiz.model.Question;
import com.miniquiz.model.Room;
import com.miniquiz.model.RoomStatus;
import com.miniquiz.model.User;
import com.miniquiz.service.PlayerService;
import com.miniquiz.service.QuestionService;
import com.miniquiz.service.RoomQuestionService;
import com.miniquiz.service.RoomService;

@Controller
public class GameController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private RoomQuestionService roomQuestionService;

    // HOST: iniciar juego y mostrar pregunta actual
    @GetMapping("/game/host/{roomId}")
    public String hostGame(@PathVariable Long roomId, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(roomId);
        if (!rOpt.isPresent()) return "redirect:/room";

        Room room = rOpt.get();
        if (room.getHost() == null || !room.getHost().getId().equals(user.getId())) {
            return "redirect:/room";
        }

        // ✅ PASAR EL TIEMPO AL MODELO (si no existe, usa 30)
        Integer seconds = 30;
        try {
            if (room.getSecondsPerQuestion() != null) seconds = room.getSecondsPerQuestion();
        } catch (Exception ignored) {}
        model.addAttribute("seconds", seconds);

        // Crear estado si no existe
        GameState state = GameManager.get(room.getPin());
        if (state == null) {

            List<Long> ids = roomQuestionService.getQuestionIdsForRoom(room.getId());

            if (ids == null || ids.isEmpty()) {
                model.addAttribute("room", room);
                model.addAttribute("msg", "Antes de empezar, selecciona las preguntas para esta sala.");
                model.addAttribute("index", 0);
                model.addAttribute("total", 0);
                model.addAttribute("hasNext", false);
                model.addAttribute("hasPrev", false);
                return "gameHost";
            }

            state = new GameState(room.getId(), room.getBlock().getId(), ids);
            GameManager.put(room.getPin(), state);

            // Asegurar estado RUNNING en BD
            room.setStatus(RoomStatus.RUNNING);
            roomService.saveRoom(room);
        }

        Long qId = state.currentQuestionId();
        if (qId == null) {
            model.addAttribute("room", room);
            model.addAttribute("msg", "No hay preguntas seleccionadas para esta sala.");
            model.addAttribute("index", 0);
            model.addAttribute("total", 0);
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrev", false);
            return "gameHost";
        }

        Optional<Question> qOpt = questionService.findQuestionById(qId);
        if (!qOpt.isPresent()) {
            model.addAttribute("room", room);
            model.addAttribute("msg", "No se pudo cargar la pregunta actual.");
            model.addAttribute("index", state.getIndex() + 1);
            model.addAttribute("total", state.getQuestionIds().size());
            model.addAttribute("hasNext", false);
            model.addAttribute("hasPrev", state.getIndex() > 0);
            return "gameHost";
        }

        model.addAttribute("room", room);
        model.addAttribute("question", qOpt.get());
        model.addAttribute("index", state.getIndex() + 1);
        model.addAttribute("total", state.getQuestionIds().size());
        model.addAttribute("hasNext", state.hasNext());
        model.addAttribute("hasPrev", state.hasPrev());

        return "gameHost";
    }

    // HOST: siguiente pregunta (si es la última -> finalizar partida)
    @GetMapping("/game/host/{roomId}/next")
    public String nextQuestion(@PathVariable Long roomId, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(roomId);
        if (!rOpt.isPresent()) return "redirect:/room";

        Room room = rOpt.get();
        if (room.getHost() == null || !room.getHost().getId().equals(user.getId())) {
            return "redirect:/room";
        }

        GameState state = GameManager.get(room.getPin());
        if (state == null) return "redirect:/game/host/" + roomId;

        if (state.hasNext()) {
            state.next();
            return "redirect:/game/host/" + roomId;
        }

        // Última -> finalizar
        room.setStatus(RoomStatus.FINISHED);
        roomService.saveRoom(room);

        return "redirect:/lobby/" + room.getPin();
    }

    // HOST: pregunta anterior
    @GetMapping("/game/host/{roomId}/prev")
    public String prevQuestion(@PathVariable Long roomId, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(roomId);
        if (!rOpt.isPresent()) return "redirect:/room";

        Room room = rOpt.get();
        if (room.getHost() == null || !room.getHost().getId().equals(user.getId())) {
            return "redirect:/room";
        }

        GameState state = GameManager.get(room.getPin());
        if (state != null && state.hasPrev()) state.prev();

        return "redirect:/game/host/" + roomId;
    }

    // PLAYER: ver pregunta actual por PIN
    @GetMapping("/game/play/{pin}")
    public String play(@PathVariable String pin, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> roomOpt = roomService.findRoomByPin(pin);
        if (!roomOpt.isPresent()) return "redirect:/join";
        Room room = roomOpt.get();

        if (room.getStatus() == RoomStatus.FINISHED) {
            return "redirect:/lobby/" + pin;
        }

        GameState state = GameManager.get(pin);
        if (state == null) {
            model.addAttribute("msg", "La partida aún no ha empezado.");
            model.addAttribute("pin", pin);
            return "gamePlay";
        }

        Long qId = state.currentQuestionId();
        if (qId == null) {
            model.addAttribute("msg", "No hay preguntas disponibles.");
            model.addAttribute("pin", pin);
            return "gamePlay";
        }

        Optional<Question> qOpt = questionService.findQuestionById(qId);
        if (!qOpt.isPresent()) {
            model.addAttribute("msg", "No se pudo cargar la pregunta.");
            model.addAttribute("pin", pin);
            return "gamePlay";
        }

        boolean alreadyAnswered = false;
        Optional<Player> pOpt = playerService.findByRoomIdAndUserId(room.getId(), user.getId());
        if (pOpt.isPresent()) {
            Player p = pOpt.get();
            alreadyAnswered = (p.getLastAnsweredQuestionId() != null && p.getLastAnsweredQuestionId().equals(qId));
        }

        model.addAttribute("pin", pin);
        model.addAttribute("question", qOpt.get());
        model.addAttribute("index", state.getIndex() + 1);
        model.addAttribute("total", state.getQuestionIds().size());
        model.addAttribute("alreadyAnswered", alreadyAnswered);

        return "gamePlay";
    }

    // PLAYER: enviar respuesta (✅ 1 sola vez por pregunta)
    @Transactional
    @PostMapping("/game/answer/{pin}")
    public String submitAnswer(@PathVariable String pin,
                              @RequestParam("option") String option,
                              @RequestParam("questionId") Long questionId,
                              HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> roomOpt = roomService.findRoomByPin(pin);
        if (!roomOpt.isPresent()) return "redirect:/join";
        Room room = roomOpt.get();

        GameState state = GameManager.get(pin);
        if (state == null) return "redirect:/game/play/" + pin;

        Long currentQId = state.currentQuestionId();
        if (currentQId == null) return "redirect:/game/play/" + pin;

        if (!currentQId.equals(questionId)) {
            return "redirect:/game/play/" + pin;
        }

        Optional<Player> pOpt = playerService.findByRoomIdAndUserIdForUpdate(room.getId(), user.getId());
        if (!pOpt.isPresent()) return "redirect:/join";
        Player player = pOpt.get();

        if (player.getLastAnsweredQuestionId() != null && player.getLastAnsweredQuestionId().equals(currentQId)) {
            return "redirect:/game/play/" + pin;
        }

        Optional<Question> qOpt = questionService.findQuestionById(currentQId);
        if (!qOpt.isPresent()) return "redirect:/game/play/" + pin;

        Question q = qOpt.get();
        boolean correct = option.equalsIgnoreCase(q.getCorrectOption());

        if (correct) {
            player.setScore(player.getScore() + 1);
        }

        player.setLastAnsweredQuestionId(currentQId);
        playerService.savePlayer(player);

        return "redirect:/game/play/" + pin;
    }
}
