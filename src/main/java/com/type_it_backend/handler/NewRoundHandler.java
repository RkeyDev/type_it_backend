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

    public static void handle(Room room) {
        if (room == null || !room.isInGame()) return;

        synchronized (room) { // Thread-safe for this room
            // Cancel previous scheduled round
            cleanAllSchedules(room.getRoomCode());

            // Reset topic
            room.setCurrentTopic("");

            JsonFileHandler jsonHandler = new JsonFileHandler(JsonFilePath.WORDS_FILE);
            String[] allTopics = jsonHandler.getAllKeys();

            // Pick a new topic
            String randomTopic = allTopics[(int) (Math.random() * allTopics.length)];
            room.setCurrentTopic(randomTopic);

            // Reset players' submission status
            room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));

            // Get question
            String question = jsonHandler.getValue(randomTopic).get("question").asText();

            // Broadcast round start
            room.broadcastResponse(ResponseFactory.startNewRoundResponse(question));

            // Schedule next round after typing time
            int timeLeft = room.getTypingTime(); // seconds
            ScheduledFuture<?> future = scheduler.schedule(() -> handle(room), timeLeft, TimeUnit.SECONDS);
            roomSchedules.put(room.getRoomCode(), future);
        }
    }

    public static void handleAllPlayersGuessed(String roomCode) {
        Room room = RoomManager.getRoomByCode(roomCode);
        if (room == null || !room.isInGame()) return;

        synchronized (room) {
            // Cancel scheduled next round
            cleanAllSchedules(roomCode);

            // Reset topic and player states
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
            System.out.println("Cleaned all schedules for room: " + roomCode);
        }
    }
}
