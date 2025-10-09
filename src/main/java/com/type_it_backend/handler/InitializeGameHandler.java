package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;
import com.type_it_backend.utils.SchedulerProvider;

public class InitializeGameHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        System.out.println("INSIDE INITIALIZE FUNCTION");
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        System.out.println("GOING GOOD");
        if (room == null) {
            System.out.println("GOING VERY BAD");
            System.out.println(room);
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        System.out.println("Initializing game for room: " + roomCode);

        // Reset room state
        room.setInGame(false);
        room.setCurrentTopic("");
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(player -> {
            player.setHasSubmittedCorrectWord(false);
            player.setGussedCharacters(0);
        });

        // Cancel any scheduled rounds
        NewRoundHandler.cleanAllSchedules(roomCode);

        // Send game settings to clients
        room.broadcastResponse(ResponseFactory.startGameResponse(room));
        System.out.println("Sent start_game response");

        // Give a short buffer for clients to finish connecting
        SchedulerProvider.SCHEDULER.schedule(() -> {
            if (room.getPlayers().isEmpty()) {
                System.out.println("Room " + roomCode + " has no players yet, aborting countdown");
                return;
            }

            room.setInGame(true);
            long now = System.currentTimeMillis();
            int countdownDurationMs = 5000; // 5s countdown
            room.broadcastResponse(ResponseFactory.countdownStartResponse(now, countdownDurationMs));
            System.out.println("Broadcasting countdown_start to room " + roomCode);

            // Schedule the round start after countdown
            SchedulerProvider.SCHEDULER.schedule(() -> {
                System.out.println("Countdown finished, starting first round for room " + roomCode);
                NewRoundHandler.handle(room);
            }, countdownDurationMs, TimeUnit.MILLISECONDS);

        }, 200, TimeUnit.MILLISECONDS); // 200ms buffer for connections
    }
}
