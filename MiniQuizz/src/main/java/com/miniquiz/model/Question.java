package com.miniquiz.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "questions")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El enunciado es obligatorio")
    @Size(max = 500, message = "El enunciado no puede superar 500 caracteres")
    @Column(nullable = false, length = 500)
    private String statement;

    @NotBlank(message = "La opción A es obligatoria")
    @Size(max = 255, message = "La opción A no puede superar 255 caracteres")
    @Column(nullable = false, length = 255)
    private String optionA;

    @NotBlank(message = "La opción B es obligatoria")
    @Size(max = 255, message = "La opción B no puede superar 255 caracteres")
    @Column(nullable = false, length = 255)
    private String optionB;

    @NotBlank(message = "La opción C es obligatoria")
    @Size(max = 255, message = "La opción C no puede superar 255 caracteres")
    @Column(nullable = false, length = 255)
    private String optionC;

    @NotBlank(message = "La opción D es obligatoria")
    @Size(max = 255, message = "La opción D no puede superar 255 caracteres")
    @Column(nullable = false, length = 255)
    private String optionD;

    /**
     * Guardamos la correcta como "A", "B", "C" o "D".
     * Pattern asegura que solo se pueda guardar uno de esos valores.
     */
    @NotBlank(message = "Debes indicar la opción correcta (A, B, C o D)")
    @Pattern(regexp = "^[ABCD]$", message = "La opción correcta debe ser A, B, C o D")
    @Column(nullable = false, length = 1)
    private String correctOption;

    /**
     * Relación con Block: muchas preguntas pertenecen a un bloque.
     * fetch LAZY para no cargar el bloque entero siempre.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    public Question() {
    }

    public Question(String statement, String optionA, String optionB, String optionC, String optionD, String correctOption, Block block) {
        this.statement = statement;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.block = block;
    }

    //GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
