package com.type_it_backend.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.JsonFilePath;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;
import com.type_it_backend.utils.SchedulerProvider;

public class NewRoundHandler {

    private static final ConcurrentHashMap<String, ScheduledFuture<?>> roomSchedules = new ConcurrentHashMap<>();

    public static void handle(Room room) {
        System.out.println("NEW ROUND IS STARTING");
        if (room == null || !room.isInGame() || !RoomManager.isRoomExists(room.getRoomCode())) return;
        System.out.println("[[NEW ROUND HAS STARTED]]");
        
        // Cancel any previously scheduled round for this room
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
        System.out.println("QUESTION: " + question);

        if (room.isInGame()) {
            // Broadcast new round info
            room.broadcastResponse(ResponseFactory.startNewRoundResponse(question));

            // Start round timer
            long roundDurationMs = room.getTypingTime() * 1000L;
            room.broadcastResponse(ResponseFactory.timerStartResponse(System.currentTimeMillis(), (int) roundDurationMs));

            // Schedule next round
            ScheduledFuture<?> future = SchedulerProvider.SCHEDULER.schedule(() -> {
                handle(room);
            }, roundDurationMs, TimeUnit.MILLISECONDS);

            roomSchedules.put(room.getRoomCode(), future);
        }
    }

    public static void handleAllPlayersGuessed(String roomCode) {
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null || !room.isInGame()) return;

        cleanAllSchedules(roomCode);

        room.setCurrentTopic("");
        room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));
        room.getCurrentWinners().clear();

        handle(room);
    }

    public static void cleanAllSchedules(String roomCode) {
        ScheduledFuture<?> future = roomSchedules.get(roomCode);
        if (future != null && !future.isDone()) {
            future.cancel(true);
            roomSchedules.remove(roomCode);
        }
    }
}
