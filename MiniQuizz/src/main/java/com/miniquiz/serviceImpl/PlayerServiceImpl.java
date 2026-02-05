package com.miniquiz.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miniquiz.model.Player;
import com.miniquiz.repository.PlayerRepo;
import com.miniquiz.service.PlayerService;

@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerRepo playerRepo;

    @Override
    public List<Player> findPlayersByRoomId(Long roomId) {
        return playerRepo.findPlayersByRoomId(roomId);
    }

    @Override
    public List<Player> findConnectedPlayersByRoomId(Long roomId) {
        return playerRepo.findConnectedPlayersByRoomId(roomId);
    }

    @Override
    public Optional<Player> findByRoomIdAndUserId(Long roomId, Long userId) {
        return playerRepo.findByRoomIdAndUserId(roomId, userId);
    }

    @Override
    public Player savePlayer(Player p) {
        if (p == null) return new Player();
        return playerRepo.save(p);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepo.deletePlayerById(id);
    }

    @Override
    public Long countPlayersByRoomId(Long roomId) {
        Long n = playerRepo.countPlayersByRoomId(roomId);
        return (n == null ? 0L : n);
    }

    @Override
    public Long countConnectedPlayersByRoomId(Long roomId) {
        Long n = playerRepo.countConnectedPlayersByRoomId(roomId);
        return (n == null ? 0L : n);
    }

    @Override
    public void deletePlayersByRoomId(Long roomId) {
        playerRepo.deleteByRoomId(roomId);
    }

    @Override
    public List<Player> findPlayersByRoomIdOrderByScoreDesc(Long roomId) {
        return playerRepo.findPlayersByRoomIdOrderByScoreDesc(roomId);
    }
    
    @Override
    public Optional<Player> findByRoomIdAndUserIdForUpdate(Long roomId, Long userId) {
        return playerRepo.findByRoomIdAndUserIdForUpdate(roomId, userId);
    }

}
