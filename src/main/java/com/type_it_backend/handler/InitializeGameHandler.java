package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class InitializeGameHandler {

    // Keep track of per-room threads
    private static final HashMap<String, Thread> roomThreads = new HashMap<>();

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

        // Interrupt any previous thread for this room
        Thread prevThread = roomThreads.get(roomCode);
        if (prevThread != null && prevThread.isAlive()) {
            prevThread.interrupt();
        }

        room.setInGame(true);
        room.broadcastResponse(ResponseFactory.startGameResponse(room));

        // Start a dedicated non-daemon thread for the new round
        Thread roomThread = new Thread(() -> {
            try {
                Thread.sleep(6000); // wait 6 seconds before starting round
                System.out.println("=== Starting New Round for room: " + room.getRoomCode() + " ===");
                System.out.println("Room state: " + room);
                System.out.println("InGame: " + room.isInGame());
                System.out.println("Room exists: " + RoomManager.isRoomExists(room.getRoomCode()));
                System.out.flush();

                NewRoundHandler.handle(room);
            } catch (InterruptedException e) {
                System.out.println("Room thread interrupted for room: " + room.getRoomCode());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.flush();
            }
        });
        roomThread.setDaemon(false); // critical for cloud hosting
        roomThread.start();

        roomThreads.put(roomCode, roomThread);
    }

}