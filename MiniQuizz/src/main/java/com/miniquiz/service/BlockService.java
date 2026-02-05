package com.miniquiz.service;

import java.util.List;
import java.util.Optional;

import com.miniquiz.model.Block;
import com.miniquiz.model.User;

public interface BlockService {

    public List<Block> findAllBlocks();

    public Optional<Block> findBlockById(Long id);

    public Block saveBlock(Block newBlock);

    public String deleteBlock(Long id);

    public String updateBlock(Block newBlock);

    // Para comprobar si un bloque puede usarse en una sala (>= 20 preguntas)
    public boolean canUseInRoom(Long blockId);
    
    List<Block> findBlocksByOwner(User owner);
    
    List<Block> findUsableBlocksByOwner(User owner, int minQuestions);
}
