package com.type_it_backend.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.JsonFilePath;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class NewRoundHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public static void handle(String roomCode) {
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null || !room.isInGame()) return;

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

            // Broadcast to clients
            room.broadcastResponse(ResponseFactory.startNewRoundResponse(question));

            // Schedule next round after typing time
            int timeLeft = room.getTypingTime(); // seconds
            scheduler.schedule(() -> {
                room.setCurrentTopic("");
                handle(roomCode); // start next round
            }, timeLeft, TimeUnit.SECONDS);

        } else {
            System.out.println("Round already active for room " + roomCode + ", skipping.");
        }
    }
}
