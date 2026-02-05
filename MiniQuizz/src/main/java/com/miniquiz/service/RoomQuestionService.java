package com.miniquiz.service;

import java.util.List;

public interface RoomQuestionService {

    List<Long> getQuestionIdsForRoom(Long roomId);

    void setQuestionsForRoom(Long roomId, List<Long> questionIds);

    void setRandomQuestionsForRoom(Long roomId, int count);
}
