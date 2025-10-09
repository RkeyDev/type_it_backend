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

    // Each room gets its own scheduler to avoid cloud thread starvation
    private static final HashMap<String, ScheduledExecutorService> roomSchedulers = new HashMap<>();

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        // Reset room for new game
        room.setInGame(false);
        room.setCurrentTopic("");
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(player -> {
            player.setHasSubmittedCorrectWord(false);
            player.setGussedCharacters(0);
        });

        // Cancel previous scheduled tasks for this room if any
        ScheduledExecutorService prevScheduler = roomSchedulers.get(roomCode);
        if (prevScheduler != null && !prevScheduler.isShutdown()) {
            prevScheduler.shutdownNow();
        }

        // Create a dedicated scheduler for this room
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        roomSchedulers.put(roomCode, scheduler);

        room.setInGame(true);
        room.broadcastResponse(ResponseFactory.startGameResponse(room));

        // Schedule the new round
        scheduler.schedule(() -> {
            try {
                System.out.println("=== Starting New Round for room: " + room.getRoomCode() + " ===");
                System.out.println("Room state: " + room);
                System.out.println("InGame: " + room.isInGame());
                System.out.println("Room exists: " + RoomManager.isRoomExists(room.getRoomCode()));
                System.out.flush();

                NewRoundHandler.handle(room);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.flush();
            }
        }, 6, TimeUnit.SECONDS);
    }

    // Cleanup scheduler when room is removed
    public static void cleanupScheduler(String roomCode) {
        ScheduledExecutorService scheduler = roomSchedulers.remove(roomCode);
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
