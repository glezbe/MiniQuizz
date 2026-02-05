package com.miniquiz.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miniquiz.dto.RoomDraft;
import com.miniquiz.model.Block;
import com.miniquiz.model.Player;
import com.miniquiz.model.Room;
import com.miniquiz.model.RoomStatus;
import com.miniquiz.model.User;
import com.miniquiz.service.BlockService;
import com.miniquiz.service.PlayerService;
import com.miniquiz.service.QuestionService;
import com.miniquiz.service.RoomQuestionService;
import com.miniquiz.service.RoomService;

@Controller
public class RoomController {

    @Autowired private RoomService roomService;
    @Autowired private BlockService blockService;
    @Autowired private PlayerService playerService;
    @Autowired private QuestionService questionService;
    @Autowired private RoomQuestionService roomQuestionService;

    //LISTADO DE SALAS
    @GetMapping("/room")
    public String rooms(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Room> myRooms = roomService.findRoomsByHost(user);
        model.addAttribute("rooms", myRooms);
        return "rooms";
    }

    //FORM ELEGIR BLOQUE
    @GetMapping("/room/add")
    public String addRoom(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        //limpiar borrador anterior
        session.removeAttribute("roomDraft");

        List<Block> myBlocks = blockService.findUsableBlocksByOwner(user, 20);
        model.addAttribute("blocks", myBlocks);

        return "roomForm";
    }

    //GUARDAR BLOQUE EN BORRADOR
    // (El botón del form debe decir "Elegir preguntas")
    @PostMapping("/room/draft/block")
    public String saveDraftBlock(@RequestParam("blockId") Long blockId,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Block> bOpt = blockService.findBlockById(blockId);
        if (!bOpt.isPresent()
                || bOpt.get().getOwner() == null
                || !bOpt.get().getOwner().getId().equals(user.getId())) {

            ra.addFlashAttribute("error", "Bloque inválido.");
            return "redirect:/room/add";
        }

        RoomDraft draft = new RoomDraft();
        draft.setBlockId(blockId);
        draft.setSecondsPerQuestion(30);
        session.setAttribute("roomDraft", draft);

        return "redirect:/room/draft/questions";
    }

    //PANTALLA TIEMPO
    @GetMapping("/room/draft/time")
    public String timeConfig(HttpSession session, Model model, RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        RoomDraft draft = (RoomDraft) session.getAttribute("roomDraft");
        if (draft == null || draft.getBlockId() == null) return "redirect:/room/add";

        if (draft.getQuestionIds() == null || draft.getQuestionIds().isEmpty()) {
            ra.addFlashAttribute("error", "Primero selecciona preguntas.");
            return "redirect:/room/draft/questions";
        }

        model.addAttribute("seconds", draft.getSecondsPerQuestion());
        return "roomTimeConfig";
    }

    //CREAR SALA FINAL
    @PostMapping("/room/create")
    public String createRoomFinal(@RequestParam("secondsPerQuestion") int secondsPerQuestion,
                                  HttpSession session,
                                  RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        RoomDraft draft = (RoomDraft) session.getAttribute("roomDraft");
        if (draft == null || draft.getBlockId() == null) return "redirect:/room/add";

        if (draft.getQuestionIds() == null || draft.getQuestionIds().isEmpty()) {
            ra.addFlashAttribute("error", "Debes seleccionar preguntas antes de crear la sala.");
            return "redirect:/room/draft/questions";
        }

        // validar segundos (ej 5..300)
        if (secondsPerQuestion < 5) secondsPerQuestion = 5;
        if (secondsPerQuestion > 300) secondsPerQuestion = 300;

        Block block = blockService.findBlockById(draft.getBlockId()).orElse(null);
        if (block == null) return "redirect:/room/add";

        Room r = new Room();
        r.setHost(user);
        r.setBlock(block);
        r.setStatus(RoomStatus.WAITING);
        r.setPin(roomService.generateUniquePin());

        r.setSecondsPerQuestion(secondsPerQuestion);

        Room saved = roomService.saveRoom(r);

        // guardar preguntas seleccionadas para esa sala
        roomQuestionService.setQuestionsForRoom(saved.getId(), draft.getQuestionIds());

        // limpiar borrador
        session.removeAttribute("roomDraft");

        ra.addFlashAttribute("ok", "Sala creada correctamente.");
        return "redirect:/room/" + saved.getId();
    }

    //VER SALA (HOST)
    @GetMapping("/room/{id}")
    public String roomById(@PathVariable Long id, Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(id);

        if (!rOpt.isPresent()
                || rOpt.get().getHost() == null
                || !rOpt.get().getHost().getId().equals(user.getId())) {
            return "redirect:/room";
        }

        Room room = rOpt.get();

        List<Player> players;
        int numPlayers;

        if (room.getStatus() == RoomStatus.FINISHED) {
            players = playerService.findPlayersByRoomIdOrderByScoreDesc(room.getId());
            numPlayers = players.size();
        } else {
            players = playerService.findConnectedPlayersByRoomId(room.getId());
            numPlayers = playerService.countConnectedPlayersByRoomId(room.getId()).intValue();
        }

        model.addAttribute("room", room);
        model.addAttribute("players", players);
        model.addAttribute("numPlayers", numPlayers);

        int totalQuestions = questionService.findQuestionsByBlock(room.getBlock().getId()).size();
        model.addAttribute("totalQuestions", totalQuestions);

        int selected = roomQuestionService.getQuestionIdsForRoom(room.getId()).size();
        model.addAttribute("selectedQuestions", selected);

        return "room";
    }

    //BORRAR SALA
    @GetMapping("/room/delete/{id}")
    public String deleteRoom(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(id);

        if (!rOpt.isPresent()
                || rOpt.get().getHost() == null
                || !rOpt.get().getHost().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "No tienes permiso para eliminar esa sala.");
            return "redirect:/room";
        }

        Long connected = playerService.countConnectedPlayersByRoomId(id);
        if (connected != null && connected > 0) {
            ra.addFlashAttribute("error", "No se puede eliminar la sala: hay jugadores conectados.");
            return "redirect:/room";
        }

        try {
            playerService.deletePlayersByRoomId(id);
            roomService.deleteRoom(id);
            ra.addFlashAttribute("ok", "Sala eliminada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se ha podido eliminar la sala.");
        }

        return "redirect:/room";
    }

    //START / FINISH / CLOSE / EXPEL (igual que tú los tenías)

    @GetMapping("/room/start/{id}")
    public String startRoom(@PathVariable Long id, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(id);
        if (!rOpt.isPresent()
                || rOpt.get().getHost() == null
                || !rOpt.get().getHost().getId().equals(user.getId())) {
            return "redirect:/room";
        }

        Room room = rOpt.get();
        room.setStatus(RoomStatus.RUNNING);
        roomService.saveRoom(room);

        return "redirect:/game/host/" + id;
    }

    @GetMapping("/room/finish/{id}")
    public String finishRoom(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(id);
        if (!rOpt.isPresent()) return "redirect:/room";

        Room room = rOpt.get();

        if (room.getHost() == null || !room.getHost().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "No tienes permiso para terminar esta sala.");
            return "redirect:/room";
        }

        room.setStatus(RoomStatus.FINISHED);
        roomService.saveRoom(room);

        ra.addFlashAttribute("ok", "Partida finalizada. Puedes ver los resultados.");
        return "redirect:/lobby/" + room.getPin();
    }

    @GetMapping("/room/close/{id}")
    public String closeRoom(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(id);
        if (!rOpt.isPresent()) return "redirect:/room";

        Room room = rOpt.get();

        if (room.getHost() == null || !room.getHost().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "No tienes permiso para cerrar esta sala.");
            return "redirect:/room";
        }

        room.setStatus(RoomStatus.FINISHED);
        roomService.saveRoom(room);

        playerService.deletePlayersByRoomId(room.getId());

        ra.addFlashAttribute("ok", "Sala cerrada. Jugadores expulsados.");
        return "redirect:/room";
    }

    @GetMapping("/room/expel/{id}")
    public String expelPlayers(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Optional<Room> rOpt = roomService.findRoomById(id);
        if (!rOpt.isPresent()) return "redirect:/room";

        Room room = rOpt.get();

        if (room.getHost() == null || !room.getHost().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "No tienes permiso para expulsar jugadores de esta sala.");
            return "redirect:/room";
        }

        playerService.deletePlayersByRoomId(room.getId());

        ra.addFlashAttribute("ok", "Jugadores expulsados. Ya puedes eliminar la sala si quieres.");
        return "redirect:/room";
    }
}
