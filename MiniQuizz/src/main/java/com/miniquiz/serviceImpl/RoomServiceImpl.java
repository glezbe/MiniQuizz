package com.miniquiz.serviceImpl;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.miniquiz.model.Room;
import com.miniquiz.model.User;
import com.miniquiz.repository.RoomRepo;
import com.miniquiz.repository.RoomQuestionRepo;
// import com.miniquiz.repository.PlayerRepo; // ✅ solo si lo tienes
import com.miniquiz.service.RoomService;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepo roomRepo;

    // ✅ NUEVO: para poder borrar las preguntas seleccionadas de la sala
    @Autowired
    private RoomQuestionRepo roomQuestionRepo;

    // ✅ opcional: si tienes playerRepo con deleteByRoomId, descomenta
    // @Autowired
    // private PlayerRepo playerRepo;

    private final SecureRandom random = new SecureRandom();

    @Override
    public List<Room> findRoomsByHost(User host) {
        return roomRepo.findRoomsByHost(host);
    }

    @Override
    public Optional<Room> findRoomById(Long id) {
        return roomRepo.findById(id);
    }

    @Override
    public Optional<Room> findRoomByPin(String pin) {
        return roomRepo.findByPin(pin);
    }

    @Override
    public Room saveRoom(Room room) {
        if (room == null) return new Room();
        return roomRepo.save(room);
    }

    /**
     * ✅ BORRADO EN CASCADA (manual):
     * - Borra primero room_question (si no, la FK te impide borrar Room)
     * - (Opcional) borra jugadores de la sala
     * - Luego borra Room
     */
    @Override
    @Transactional
    public String deleteRoom(Long id) {
        if (id == null) return "Id inválido";

        Optional<Room> r = roomRepo.findById(id);
        if (!r.isPresent()) return "La sala no existe";

        // ✅ primero borrar dependencias
        roomQuestionRepo.deleteByRoomId(id);

        // ✅ opcional: si tienes este método, borra jugadores
        // playerRepo.deleteByRoomId(id);

        // ✅ luego borrar sala
        roomRepo.deleteById(id);

        return "Sala eliminada";
    }

    @Override
    public String generateUniquePin() {
        // 6 dígitos, como Kahoot (puedes cambiar a 8 si quieres)
        String pin;
        do {
            pin = String.format("%06d", random.nextInt(1_000_000));
        } while (roomRepo.existsByPin(pin));
        return pin;
    }

    @Override
    public boolean existsByBlockId(Long blockId) {
        return roomRepo.existsByBlockId(blockId);
    }
}
