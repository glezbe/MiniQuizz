package com.miniquiz.service;

import java.util.Optional;

import com.miniquiz.model.User;

public interface UserService {

    public Optional<User> findByUsername(String username);

    public User registerUser(User newUser);

    public Optional<User> login(String username, String password);
}
