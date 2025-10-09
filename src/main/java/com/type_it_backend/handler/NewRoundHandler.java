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
        if (room == null || !room.isInGame() || !RoomManager.isRoomExists(room.getRoomCode())) return;

        // Cancel any previous scheduled round for this room
        cleanAllSchedules(room.getRoomCode());

        // Reset topic
        room.setCurrentTopic("");

        JsonFileHandler jsonHandler = new JsonFileHandler(JsonFilePath.WORDS_FILE);
        String[] allTopics = jsonHandler.getAllKeys();

        // Pick a new topic only if none is active
        if (room.getCurrentTopic() == null || room.getCurrentTopic().isEmpty()) {
            String randomTopic = allTopics[(int) (Math.random() * allTopics.length)];
            room.setCurrentTopic(randomTopic);

            // Reset players' submission status
            room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));

            // Get question for the round
            String question = jsonHandler.getValue(randomTopic).get("question").asText();

            if (room.isInGame()) {
                // Start new round 
                room.broadcastResponse(ResponseFactory.startNewRoundResponse(question));

                // Schedule next round after typing time
                int timeLeft = room.getTypingTime(); // seconds
                ScheduledFuture<?> future = scheduler.schedule(() -> {
                    handle(room); // start next round
                }, timeLeft, TimeUnit.SECONDS);

                // Store the scheduled task for this room
                roomSchedules.put(room.getRoomCode(), future);
            }

        } else {
            System.out.println("Round already active for room " + room.getRoomCode() + ", skipping.");
        }
    }

    /**
     * Handles starting a new round when all players have guessed correctly
     * @param roomCode The room code to start new round for
     */
    public static void handleAllPlayersGuessed(String roomCode) {
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null || !room.isInGame()) return;

        // Cancel any scheduled next round to avoid double triggers
        cleanAllSchedules(roomCode);

        // Clear current topic to allow new round
        room.setCurrentTopic("");

        // Reset players' submission status
        room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));

        // Clear current winners
        room.getCurrentWinners().clear();

        // Start new round
        handle(room);
    }

    /**
     * Cleans all scheduled tasks for a specific room
     * @param roomCode The room code to clean schedules for
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
