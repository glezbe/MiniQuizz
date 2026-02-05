package com.miniquiz.concurrency;

import java.util.List;

public class GameState {
    private final Long roomId;
    private final Long blockId;
    private final List<Long> questionIds;
    private int index = 0;


    public GameState(Long roomId, Long blockId, List<Long> questionIds) {
        this.roomId = roomId;
        this.blockId = blockId;
        this.questionIds = questionIds;
        this.index = 0;
    }

    public Long getRoomId() { return roomId; }
    public Long getBlockId() { return blockId; }
    public List<Long> getQuestionIds() { return questionIds; }
    public boolean hasNext() { return questionIds != null && index < questionIds.size() - 1; }
    public boolean hasPrev() { return questionIds != null && index > 0; }
    public void prev() { if (hasPrev()) index--; }


    public int getIndex() { return index; }
    public void next() {
        if (index < questionIds.size() - 1) index++;
    }

    public Long currentQuestionId() {
        if (questionIds == null || questionIds.isEmpty()) return null;
        return questionIds.get(index);
    }

    public boolean isFinished() {
        return questionIds == null || questionIds.isEmpty() || index >= questionIds.size();
    }
}
