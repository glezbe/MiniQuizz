package com.miniquiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.miniquiz.model.Room;
import com.miniquiz.model.User;

public interface RoomRepo extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.host = ?1 ORDER BY r.id DESC")
    List<Room> findRoomsByHost(User host);

	@Query("SELECT r FROM Room r WHERE r.pin = ?1")
    Optional<Room> findByPin(String pin);
	
    @Query("SELECT COUNT(r) > 0 FROM Room r WHERE r.pin = ?1")
    boolean existsByPin(String pin);

    @Query("SELECT r FROM Room r ORDER BY r.id DESC")
    List<Room> findAllOrdered();
    
    @Query("SELECT r FROM Room r WHERE r.status = ?1")
    List<Room> findByStatus(String status);
    
    boolean existsByBlockId(Long blockId);
}
