package com.miniquiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.miniquiz.model.Block;
import com.miniquiz.model.User;

public interface BlockRepo extends JpaRepository<Block, Long> {

    // Lista todos los bloques ordenados por nombre (útil para "Mis Bloques")
    @Query("SELECT b FROM Block b ORDER BY b.name ASC")
    List<Block> findAllOrderedByName();

    // Buscar por nombre exacto
    @Query("SELECT b FROM Block b WHERE b.name = ?1")
    List<Block> findByName(String name);

    // Buscar por id (esto ya lo trae JpaRepository)
    @Query("SELECT b FROM Block b WHERE b.id = ?1")
    Optional<Block> findBlockById(Long id);
    
    //buscar por user 
    @Query("SELECT b FROM Block b WHERE b.owner = ?1")
    List<Block> findByOwner(User owner);
    
    @Query("SELECT b FROM Block b WHERE b.owner = ?1 AND (SELECT COUNT(q) FROM Question q WHERE q.block = b) >= ?2 ORDER BY b.name ASC")
    List<Block> findUsableBlocksByOwner(User owner, Long minQuestions);

}
