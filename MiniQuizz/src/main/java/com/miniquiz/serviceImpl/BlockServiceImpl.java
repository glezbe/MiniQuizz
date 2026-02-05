package com.miniquiz.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miniquiz.model.Block;
import com.miniquiz.repository.BlockRepo;
import com.miniquiz.repository.QuestionRepo;
import com.miniquiz.service.BlockService;
import com.miniquiz.model.User;

@Service
public class BlockServiceImpl implements BlockService {

    @Autowired
    private BlockRepo blockRepositorio;

    @Autowired
    private QuestionRepo questionRepositorio;

    @Override
    public List<Block> findAllBlocks() {
        return blockRepositorio.findAll();
    }

    @Override
    public Optional<Block> findBlockById(Long id) {
        return blockRepositorio.findById(id);
    }

    @Override
    public Block saveBlock(Block newBlock) {
        Block b;
        if (newBlock != null) {
            b = blockRepositorio.save(newBlock);
        } else {
            b = new Block();
        }
        return b;
    }

    @Override
    public String deleteBlock(Long id) {
        Optional<Block> b = blockRepositorio.findById(id);
        if (b.isPresent()) {
            blockRepositorio.deleteById(id);
            return "Bloque eliminado satisfactoriamente";
        }
        return "El bloque no existe";
    }

    @Override
    public String updateBlock(Block blockActualizar) {
        if (blockActualizar != null
                && blockActualizar.getId() != null
                && blockRepositorio.findById(blockActualizar.getId()).isPresent()) {

            blockRepositorio.save(blockActualizar);
            return "Bloque " + blockActualizar.getId() + " actualizado";
        } else {
            return "No se ha podido actualizar el bloque";
        }
    }

    /**
     * Regla del enunciado:
     * Un bloque solo puede usarse en una sala si tiene 20 o más preguntas
     */
    @Override
    public boolean canUseInRoom(Long blockId) {
        Long numPreguntas = questionRepositorio.countQuestionsByBlockId(blockId);
        return numPreguntas != null && numPreguntas >= 20;
    }
    
    @Override
    public List<Block> findBlocksByOwner(User owner) {
        return blockRepositorio.findByOwner(owner);
    }
    
    @Override
    public List<Block> findUsableBlocksByOwner(User owner, int minQuestions) {
        return blockRepositorio.findUsableBlocksByOwner(owner, (long) minQuestions);
    }

}
