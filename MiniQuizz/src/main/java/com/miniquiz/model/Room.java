package com.miniquiz.model;

import java.io.Serializable;
import javax.persistence.*;

@Entity
@Table(name = "rooms")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PIN de acceso público (único)
    @Column(nullable = false, unique = true, length = 10)
    private String pin;

    // Host (usuario que crea la sala)
    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    // Bloque de preguntas que se jugará en la sala
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    // ✅ NUEVO: segundos por pregunta (configurable, por defecto 30)
    @Column(name = "seconds_per_question", nullable = false)
    private Integer secondsPerQuestion = 30;

    public Room() {}

    public Room(String pin, User host, Block block, RoomStatus status) {
        this.pin = pin;
        this.host = host;
        this.block = block;
        this.status = status;
        this.secondsPerQuestion = 30;
    }

    // Getters / Setters
    public Long getId() {
        return id;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public Integer getSecondsPerQuestion() {
        return secondsPerQuestion;
    }

    public void setSecondsPerQuestion(Integer secondsPerQuestion) {
        this.secondsPerQuestion = secondsPerQuestion;
    }
}
