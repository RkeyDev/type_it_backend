package com.type_it_backend.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.DatabaseManager;
import com.type_it_backend.utils.ResponseFactory;

public class NewRoundHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> roomSchedules = new ConcurrentHashMap<>();

    public static void handle(Room room) {
        System.out.println("INGAME: " + room.isInGame());
        System.out.println("Is ROom Exists: " + RoomManager.isRoomExists(room.getRoomCode()));
        if (room == null || !room.isInGame() || !RoomManager.isRoomExists(room.getRoomCode())) return;

        System.out.println("Starting round!");
        // Cancel any previous scheduled round for this room
        cleanAllSchedules(room.getRoomCode());



            // Reset players' submission status
            room.getPlayers().values().forEach(player -> player.setHasSubmittedCorrectWord(false));

            
            if (room.isInGame()) {
                // Start new round 
                new Thread(()->{room.updateAllPossibleAnswers();}).start();
                room.broadcastResponse(ResponseFactory.startNewRoundResponse(room.getCurrentQuestion()));

                // Schedule next round after typing time
                int timeLeft = room.getTypingTime(); // seconds

                // Pick a new topic only if none is active
                String randomQuestion = DatabaseManager.getRandomQuestion();
                room.setCurrentQuestion(randomQuestion);
                
                

                ScheduledFuture<?> future = scheduler.schedule(() -> {
                    handle(room); // start next round
                }, timeLeft, TimeUnit.SECONDS);

                // Store the scheduled task for this room
                roomSchedules.put(room.getRoomCode(), future);
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
