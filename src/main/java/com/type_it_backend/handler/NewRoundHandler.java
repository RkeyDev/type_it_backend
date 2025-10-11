package com.type_it_backend.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class NewRoundHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> roomSchedules = new ConcurrentHashMap<>();

    public static void handle(Room room) {
        if (room == null || !room.isInGame() || !RoomManager.isRoomExists(room.getRoomCode())) return;
        cleanAllSchedules(room.getRoomCode());
        room.getPlayers().values().forEach(p -> p.setHasSubmittedCorrectWord(false));

        if (room.isInGame()) {
            room.updateCurrentQustion();

            room.broadcastResponse(ResponseFactory.startNewRoundResponse(room.getCurrentQuestion()));

            int timeLeft = room.getTypingTime();
            ScheduledFuture<?> future = scheduler.schedule(() -> handle(room), timeLeft, TimeUnit.SECONDS);
            roomSchedules.put(room.getRoomCode(), future);
        }
    }

    // Used for the first round (question preloaded)
    public static void startPreloadedRound(Room room) {
        if (room == null || !room.isInGame() || !RoomManager.isRoomExists(room.getRoomCode())) return;
        cleanAllSchedules(room.getRoomCode());
        room.getPlayers().values().forEach(p -> p.setHasSubmittedCorrectWord(false));

        // Broadcast instantly (no DB call)
        room.broadcastResponse(ResponseFactory.startNewRoundResponse(room.getCurrentQuestion()));

        int timeLeft = room.getTypingTime();
        ScheduledFuture<?> future = scheduler.schedule(() -> handle(room), timeLeft, TimeUnit.SECONDS);
        roomSchedules.put(room.getRoomCode(), future);
    }

    public static void handleAllPlayersGuessed(String roomCode) {
        Room room = RoomManager.getRoomByCode(roomCode);
        if (room == null || !room.isInGame()) return;
        cleanAllSchedules(roomCode);
        room.getPlayers().values().forEach(p -> p.setHasSubmittedCorrectWord(false));
        room.getCurrentWinners().clear();
        handle(room);
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
