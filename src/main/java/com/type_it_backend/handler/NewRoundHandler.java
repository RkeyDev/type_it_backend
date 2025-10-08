package com.type_it_backend.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.JsonFilePath;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class NewRoundHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> roomSchedules = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> countdownSchedules = new ConcurrentHashMap<>();

    public static void handle(Room room) {
        if (room == null || !room.isInGame()) return;

        synchronized (room) {
            cleanAllSchedules(room.getRoomCode());

            room.setCurrentTopic("");

            JsonFileHandler jsonHandler = new JsonFileHandler(JsonFilePath.WORDS_FILE);
            String[] allTopics = jsonHandler.getAllKeys();

            String randomTopic = allTopics[(int) (Math.random() * allTopics.length)];
            room.setCurrentTopic(randomTopic);

            room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));

            String question = jsonHandler.getValue(randomTopic).get("question").asText();

            room.broadcastResponse(ResponseFactory.startNewRoundResponse(question));

            int typingTime = room.getTypingTime(); // seconds

            // Start countdown broadcast
            startCountdown(room, typingTime);

            // Schedule next round when time runs out
            ScheduledFuture<?> future = scheduler.schedule(() -> handle(room), typingTime, TimeUnit.SECONDS);
            roomSchedules.put(room.getRoomCode(), future);
        }
    }

    private static void startCountdown(Room room, int timeLeft) {
        String roomCode = room.getRoomCode();

        // Cancel previous countdown if exists
        ScheduledFuture<?> existing = countdownSchedules.get(roomCode);
        if (existing != null && !existing.isDone()) {
            existing.cancel(true);
        }

        // Countdown logic (every second)
        ScheduledFuture<?> countdownFuture = scheduler.scheduleAtFixedRate(() -> {
            int remaining = room.decrementAndGetTimeLeft();
            if (remaining >= 0) {
                room.broadcastResponse(ResponseFactory.countdownResponse(remaining));
            }
            if (remaining <= 0) {
                ScheduledFuture<?> f = countdownSchedules.remove(roomCode);
                if (f != null) f.cancel(true);
            }
        }, 0, 1, TimeUnit.SECONDS);

        countdownSchedules.put(roomCode, countdownFuture);

        // Store initial value for tracking
        room.setTimeLeft(timeLeft);
    }

    public static void handleAllPlayersGuessed(String roomCode) {
        Room room = RoomManager.getRoomByCode(roomCode);
        if (room == null || !room.isInGame()) return;

        synchronized (room) {
            cleanAllSchedules(roomCode);
            room.setCurrentTopic("");
            room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));
            room.getCurrentWinners().clear();

            handle(room);
        }
    }

    public static void cleanAllSchedules(String roomCode) {
        ScheduledFuture<?> future = roomSchedules.get(roomCode);
        if (future != null && !future.isDone()) {
            future.cancel(true);
            roomSchedules.remove(roomCode);
            System.out.println("Cleaned round schedule for room: " + roomCode);
        }

        ScheduledFuture<?> countdownFuture = countdownSchedules.get(roomCode);
        if (countdownFuture != null && !countdownFuture.isDone()) {
            countdownFuture.cancel(true);
            countdownSchedules.remove(roomCode);
            System.out.println("Cleaned countdown for room: " + roomCode);
        }
    }
}
