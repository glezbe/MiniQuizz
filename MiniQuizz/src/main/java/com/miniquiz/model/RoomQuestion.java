package com.miniquiz.model;

import javax.persistence.*;

@Entity
@Table(
    name = "room_question",
    uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "question_id"})
)
public class RoomQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    // opcional: guardar orden
    private Integer orderIndex;

    public RoomQuestion() {}

    public RoomQuestion(Room room, Question question, Integer orderIndex) {
        this.room = room;
        this.question = question;
        this.orderIndex = orderIndex;
    }

    public Long getId() { return id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
