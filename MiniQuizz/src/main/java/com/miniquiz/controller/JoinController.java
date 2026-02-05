package com.miniquiz.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.miniquiz.concurrency.GameManager;
import com.miniquiz.concurrency.GameState;
import com.miniquiz.model.Player;
import com.miniquiz.model.Room;
import com.miniquiz.model.User;
import com.miniquiz.service.PlayerService;
import com.miniquiz.service.QuestionService;
import com.miniquiz.service.RoomService;

@Controller
public class JoinController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private QuestionService questionService;

    // Formulario para unirse
    @GetMapping("/join")
    public String joinForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        return "join";
    }

    // Procesar PIN
    @PostMapping("/join")
    public String doJoin(@RequestParam("pin") String pin, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> roomOpt = roomService.findRoomByPin(pin);
        if (!roomOpt.isPresent()) {
            model.addAttribute("error", "PIN incorrecto o sala inexistente");
            return "join";
        }

        Room room = roomOpt.get();

        // Si el usuario es el host, lo mandamos a su vista de host
        if (room.getHost() != null && room.getHost().getId().equals(user.getId())) {
            return "redirect:/room/" + room.getId();
        }

        // Si no es host, lo registramos como player (o lo reactivamos)
        Optional<Player> existing = playerService.findByRoomIdAndUserId(room.getId(), user.getId());

        if (existing.isPresent()) {
            Player p = existing.get();
            p.setConnected(true); // ✅ vuelve a estar conectado
            playerService.savePlayer(p);
        } else {
            Player p = new Player(room, user, user.getUsername());
            p.setConnected(true);
            playerService.savePlayer(p);
        }

        return "redirect:/lobby/" + room.getPin();
    }

    // Lobby (jugadores en la sala)
    @GetMapping("/lobby/{pin}")
    public String lobby(@PathVariable String pin, HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> roomOpt = roomService.findRoomByPin(pin);
        if (!roomOpt.isPresent()) return "redirect:/join";

        Room room = roomOpt.get();
        boolean isHost = room.getHost() != null && room.getHost().getId().equals(user.getId());

        // Si no soy host, debo estar registrado como player en esa sala
        if (!isHost) {
            Optional<Player> pOpt = playerService.findByRoomIdAndUserId(room.getId(), user.getId());
            if (!pOpt.isPresent()) return "redirect:/join";
        }

        // ✅ Si FINISHED -> resultados (TODOS, conectados o no)
        // ✅ Si NO FINISHED -> lobby (SOLO conectados)
        List<Player> players;
        int numPlayers;

        if ("FINISHED".equals(room.getStatus().name())) {
            players = playerService.findPlayersByRoomIdOrderByScoreDesc(room.getId());
            numPlayers = players.size();
        } else {
            players = playerService.findConnectedPlayersByRoomId(room.getId());
            numPlayers = playerService.countConnectedPlayersByRoomId(room.getId()).intValue();
        }

        // total preguntas para "3 / 21"
        int totalQuestions = 0;
        GameState state = GameManager.get(room.getPin());
        if (state != null && state.getQuestionIds() != null) {
            totalQuestions = state.getQuestionIds().size();
        } else {
            totalQuestions = questionService.findQuestionsByBlock(room.getBlock().getId()).size();
        }

        model.addAttribute("room", room);
        model.addAttribute("players", players);
        model.addAttribute("isHost", isHost);
        model.addAttribute("numPlayers", numPlayers);
        model.addAttribute("totalQuestions", totalQuestions);

        return "lobby";
    }

    // Abandonar sala (jugadores en la sala)
    @GetMapping("/leave/{pin}")
    public String leaveRoom(@PathVariable String pin, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> roomOpt = roomService.findRoomByPin(pin);
        if (!roomOpt.isPresent()) return "redirect:/";

        Room room = roomOpt.get();

        // Si es el host, no se le permite salir así
        if (room.getHost() != null && room.getHost().getId().equals(user.getId())) {
            return "redirect:/room/" + room.getId();
        }

        // ✅ NO eliminar: marcar como desconectado
        Optional<Player> pOpt = playerService.findByRoomIdAndUserId(room.getId(), user.getId());
        if (pOpt.isPresent()) {
            Player p = pOpt.get();
            p.setConnected(false);
            playerService.savePlayer(p);
        }

        return "redirect:/join";
    }
}
