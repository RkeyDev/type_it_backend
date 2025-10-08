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

        // Cancel any previously scheduled round for this room
        cleanAllSchedules(room.getRoomCode());

        // Reset topic
        room.setCurrentTopic("");

        JsonFileHandler jsonHandler = new JsonFileHandler(JsonFilePath.WORDS_FILE);
        String[] allTopics = jsonHandler.getAllKeys();

        // Pick a new topic if none is active
        if (room.getCurrentTopic() == null || room.getCurrentTopic().isEmpty()) {
            String randomTopic = allTopics[(int) (Math.random() * allTopics.length)];
            room.setCurrentTopic(randomTopic);

            // Reset players' submission status
            room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));

            // Get question for the round
            String question = jsonHandler.getValue(randomTopic).get("question").asText();

            if (room.isInGame()) {
                // --- SYNC TIMING LOGIC ---
                long now = System.currentTimeMillis();
                long roundStartAt = now + 1500; // small 1.5s prep delay before question shows
                int roundDurationMs = room.getTypingTime() * 1000;

                room.broadcastResponse(ResponseFactory.startNewRoundResponse(question));

                room.broadcastResponse(ResponseFactory.timerStartResponse(roundStartAt, roundDurationMs));

                long delayMs = (roundStartAt + roundDurationMs) - now;
                ScheduledFuture<?> future = scheduler.schedule(() -> {
                    handle(room); // start next round
                }, delayMs, TimeUnit.MILLISECONDS);

                // Store scheduled future so we can cancel if needed
                roomSchedules.put(room.getRoomCode(), future);
            }

        } else {
            System.out.println("Round already active for room " + room.getRoomCode() + ", skipping.");
        }
    }

    /**
     * Called when all players have guessed correctly — forces next round immediately.
     */
    public static void handleAllPlayersGuessed(String roomCode) {
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null || !room.isInGame()) return;

        // Cancel any pending next round
        cleanAllSchedules(roomCode);

        // Reset state
        room.setCurrentTopic("");
        room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));
        room.getCurrentWinners().clear();

        // Start new round immediately
        handle(room);
    }

    /**
     * Cancels all scheduled next rounds for the given room.
     */
    public static void cleanAllSchedules(String roomCode) {
        ScheduledFuture<?> future = roomSchedules.get(roomCode);
        if (future != null && !future.isDone()) {
            future.cancel(true);
            roomSchedules.remove(roomCode);
            System.out.println("Cleaned all schedules for room: " + roomCode);
        }
    }
}
