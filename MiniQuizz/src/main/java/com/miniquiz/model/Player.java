package com.miniquiz.model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(
    name = "players",
    uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"})
)
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Column(nullable = false)
    private int score = 0;

    @Column(name = "last_answered_question_id")
    private Long lastAnsweredQuestionId;

    // ✅ NUEVO: conectado/desconectado (NO borra el player)
    @Column(nullable = false)
    private boolean connected = true;
    
    @Column(name = "last_answered_option", length = 1)
    private String lastAnsweredOption;

    public Player() {}

    public Player(Room room, User user, String displayName) {
        this.room = room;
        this.user = user;
        this.displayName = displayName;
        this.score = 0;
        this.lastAnsweredQuestionId = null;
        this.connected = true;
    }

    public Long getId() { return id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Long getLastAnsweredQuestionId() { return lastAnsweredQuestionId; }
    public void setLastAnsweredQuestionId(Long lastAnsweredQuestionId) { this.lastAnsweredQuestionId = lastAnsweredQuestionId; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public String getLastAnsweredOption() { return lastAnsweredOption; }
    public void setLastAnsweredOption(String lastAnsweredOption) { this.lastAnsweredOption = lastAnsweredOption; }
}
