package com.miniquiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import com.miniquiz.model.Player;

public interface PlayerRepo extends JpaRepository<Player, Long> {

    @Query("SELECT p FROM Player p WHERE p.room.id = ?1 ORDER BY p.id ASC")
    List<Player> findPlayersByRoomId(Long roomId);

    @Query("SELECT p FROM Player p WHERE p.room.id = ?1 AND p.user.id = ?2")
    Optional<Player> findByRoomIdAndUserId(Long roomId, Long userId);

    @Query("SELECT COUNT(p) FROM Player p WHERE p.room.id = ?1")
    Long countPlayersByRoomId(Long roomId);

    // ✅ NUEVO: contar SOLO conectados
    @Query("SELECT COUNT(p) FROM Player p WHERE p.room.id = ?1 AND p.connected = true")
    Long countConnectedPlayersByRoomId(Long roomId);

    // ✅ NUEVO: listar SOLO conectados
    @Query("SELECT p FROM Player p WHERE p.room.id = ?1 AND p.connected = true ORDER BY p.id ASC")
    List<Player> findConnectedPlayersByRoomId(Long roomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Player p WHERE p.id = ?1")
    void deletePlayerById(Long id);

    // ✅ ya la tienes: borrar todos los players de la sala (conectados y desconectados)
    @Modifying
    @Transactional
    @Query("DELETE FROM Player p WHERE p.room.id = ?1")
    void deleteByRoomId(Long roomId);

    // ranking por score
    @Query("SELECT p FROM Player p WHERE p.room.id = ?1 ORDER BY p.score DESC, p.id ASC")
    List<Player> findPlayersByRoomIdOrderByScoreDesc(Long roomId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Player p WHERE p.room.id = ?1 AND p.user.id = ?2")
    Optional<Player> findByRoomIdAndUserIdForUpdate(Long roomId, Long userId);
}
