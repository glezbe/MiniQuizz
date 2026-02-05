package com.miniquiz.concurrency;

import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private static final ConcurrentHashMap<String, GameState> gamesByPin = new ConcurrentHashMap<>();

    private GameManager() {}

    public static GameState get(String pin) {
        return gamesByPin.get(pin);
    }

    public static void put(String pin, GameState state) {
        gamesByPin.put(pin, state);
    }

    public static void remove(String pin) {
        gamesByPin.remove(pin);
    }
}
