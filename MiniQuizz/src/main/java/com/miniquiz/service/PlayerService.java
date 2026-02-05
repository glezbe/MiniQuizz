package com.miniquiz.service;

import java.util.List;
import java.util.Optional;

import com.miniquiz.model.Player;

public interface PlayerService {

    List<Player> findPlayersByRoomId(Long roomId);

    List<Player> findConnectedPlayersByRoomId(Long roomId);

    Optional<Player> findByRoomIdAndUserId(Long roomId, Long userId);

    Player savePlayer(Player p);

    void deletePlayer(Long id);

    Long countPlayersByRoomId(Long roomId);

    Long countConnectedPlayersByRoomId(Long roomId);

    void deletePlayersByRoomId(Long roomId);

    List<Player> findPlayersByRoomIdOrderByScoreDesc(Long roomId);

    Optional<Player> findByRoomIdAndUserIdForUpdate(Long roomId, Long userId);
}
