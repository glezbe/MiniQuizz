package com.miniquiz.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoomDraft implements Serializable {

    private Long blockId;
    private List<Long> questionIds = new ArrayList<>();
    private int secondsPerQuestion = 30;

    public Long getBlockId() { return blockId; }
    public void setBlockId(Long blockId) { this.blockId = blockId; }

    public List<Long> getQuestionIds() { return questionIds; }
    public void setQuestionIds(List<Long> questionIds) { this.questionIds = questionIds; }

    public int getSecondsPerQuestion() { return secondsPerQuestion; }
    public void setSecondsPerQuestion(int secondsPerQuestion) { this.secondsPerQuestion = secondsPerQuestion; }
}
