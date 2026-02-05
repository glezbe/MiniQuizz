package com.miniquiz.serviceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miniquiz.model.Question;
import com.miniquiz.model.Room;
import com.miniquiz.model.RoomQuestion;
import com.miniquiz.repository.RoomQuestionRepo;
import com.miniquiz.service.QuestionService;
import com.miniquiz.service.RoomQuestionService;
import com.miniquiz.service.RoomService;

@Service
public class RoomQuestionServiceImpl implements RoomQuestionService {

    @Autowired private RoomQuestionRepo roomQuestionRepo;
    @Autowired private RoomService roomService;
    @Autowired private QuestionService questionService;

    @Override
    public List<Long> getQuestionIdsForRoom(Long roomId) {
        return roomQuestionRepo.findByRoomIdOrderByOrderIndexAsc(roomId)
                .stream()
                .map(rq -> rq.getQuestion().getId())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setQuestionsForRoom(Long roomId, List<Long> questionIds) {

        // borrar selección previa
        roomQuestionRepo.deleteByRoomId(roomId);

        Room room = roomService.findRoomById(roomId).orElse(null);
        if (room == null) return;

        int idx = 0;
        for (Long qId : questionIds) {
            Optional<Question> qOpt = questionService.findQuestionById(qId);
            if (qOpt.isPresent()) {
                // seguridad extra: que la pregunta sea del bloque de esa sala
                if (qOpt.get().getBlock() != null
                        && room.getBlock() != null
                        && qOpt.get().getBlock().getId().equals(room.getBlock().getId())) {

                    roomQuestionRepo.save(new RoomQuestion(room, qOpt.get(), idx));
                    idx++;
                }
            }
        }
    }

    @Override
    @Transactional
    public void setRandomQuestionsForRoom(Long roomId, int count) {
        Room room = roomService.findRoomById(roomId).orElse(null);
        if (room == null || room.getBlock() == null) return;

        List<Question> all = questionService.findQuestionsByBlock(room.getBlock().getId());
        if (all.isEmpty()) return;

        // ajustar count
        if (count < 1) count = 1;
        if (count > all.size()) count = all.size();

        Collections.shuffle(all);
        List<Long> selected = new ArrayList<>();
        for (int i = 0; i < count; i++) selected.add(all.get(i).getId());

        setQuestionsForRoom(roomId, selected);
    }
}
