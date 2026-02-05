package com.miniquiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniquiz.model.RoomQuestion;

public interface RoomQuestionRepo extends JpaRepository<RoomQuestion, Long> {

    List<RoomQuestion> findByRoomIdOrderByOrderIndexAsc(Long roomId);

    void deleteByRoomId(Long roomId);

    long countByRoomId(Long roomId);
}
