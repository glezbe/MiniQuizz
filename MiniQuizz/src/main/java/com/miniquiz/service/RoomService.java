package com.miniquiz.service;

import java.util.List;
import java.util.Optional;

import com.miniquiz.model.Room;
import com.miniquiz.model.User;

public interface RoomService {

    List<Room> findRoomsByHost(User host);

    Optional<Room> findRoomById(Long id);
    
    Optional<Room> findRoomByPin(String pin);

    Room saveRoom(Room room);

    String deleteRoom(Long id);

    String generateUniquePin();
    
    boolean existsByBlockId(Long blockId);
}
