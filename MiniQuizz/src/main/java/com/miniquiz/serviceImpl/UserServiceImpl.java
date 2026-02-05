package com.miniquiz.serviceImpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miniquiz.model.User;
import com.miniquiz.repository.UserRepo;
import com.miniquiz.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public User registerUser(User newUser) {
        if (newUser == null) return new User();

        // No permitir username repetido
        Optional<User> existing = userRepo.findByUsername(newUser.getUsername());
        if (existing.isPresent()) {
            // devolvemos "vacío" para que el controller muestre error
            return new User();
        }

        return userRepo.save(newUser);
    }

    @Override
    public Optional<User> login(String username, String password) {
        Optional<User> uOpt = userRepo.findByUsername(username);

        if (!uOpt.isPresent()) return Optional.empty();

        User u = uOpt.get();

        // Comparación simple (texto plano)
        if (u.getPassword().equals(password)) {
            return Optional.of(u);
        }

        return Optional.empty();
    }
}
