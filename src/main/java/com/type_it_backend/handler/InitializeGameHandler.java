package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class InitializeGameHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        // Reset room for a fresh game
        room.setInGame(false);
        room.setCurrentTopic("");
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(player -> {
            player.setHasSubmittedCorrectWord(false);
            player.setGussedCharacters(0);
        });
        NewRoundHandler.cleanAllSchedules(room.getRoomCode());

        room.broadcastResponse(ResponseFactory.startGameResponse(room));

        long now = System.currentTimeMillis();
        int countdownDurationMs = 6000; // 6 seconds
        long countdownStartAt = now + 1000; // give clients 1s buffer before countdown starts

        room.broadcastResponse(ResponseFactory.countdownStartResponse(countdownStartAt, countdownDurationMs));

        long totalDelay = (countdownStartAt + countdownDurationMs) - now;

        scheduler.schedule(() -> {
            room.setInGame(true);
            NewRoundHandler.handle(room);
        }, totalDelay, TimeUnit.MILLISECONDS);
    }
}
