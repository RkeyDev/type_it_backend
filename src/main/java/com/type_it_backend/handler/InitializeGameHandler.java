package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.DatabaseManager;
import com.type_it_backend.utils.ResponseFactory;

public class InitializeGameHandler {

    // Thread pool for all room tasks (reuses threads efficiently)
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    // Track running tasks per room
    private static final ConcurrentHashMap<String, Future<?>> roomFutures = new ConcurrentHashMap<>();

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        // Cancel any existing running game task for this room
        Future<?> oldFuture = roomFutures.remove(roomCode);
        if (oldFuture != null && !oldFuture.isDone()) {
            oldFuture.cancel(true);
        }

        // Reset room state for a new game
        room.setInGame(false);
        room.setCurrentQuestion(null);
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(player -> {
            player.setHasSubmittedCorrectWord(false);
            player.setGussedCharacters(0);
        });

        // Start new game
        room.setInGame(true);
        room.broadcastResponse(ResponseFactory.startGameResponse(room));

        // Submit a new round initialization task
        Future<?> newFuture = executor.submit(() -> {
            try {
                // Fetch random question
                room.setCurrentQuestion(DatabaseManager.getRandomQuestion());
                new Thread(()->{room.updateAllPossibleAnswers();}).start();

                // Delay before first round begins
                Thread.sleep(6000);

                // Start the first round
                System.out.println("=== Starting New Round for room: " + room.getRoomCode() + " ===");
                System.out.println("Room state: " + room);
                System.out.println("InGame: " + room.isInGame());
                System.out.println("Room exists: " + RoomManager.isRoomExists(room.getRoomCode()));
                System.out.flush();

                NewRoundHandler.handle(room);

            } catch (InterruptedException e) {
                System.out.println("Game task interrupted for room: " + room.getRoomCode());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.flush();
            } finally {
                // Cleanup after thread finishes or is interrupted
                roomFutures.remove(room.getRoomCode());
            }
        });

        // Track this future so we can cancel or clean up later
        roomFutures.put(roomCode, newFuture);
    }

    // Optional cleanup utility if you ever remove a room
    public static void cleanupRoom(String roomCode) {
        Future<?> f = roomFutures.remove(roomCode);
        if (f != null && !f.isDone()) f.cancel(true);
    }

    // Graceful shutdown on server stop
    public static void shutdown() {
        executor.shutdownNow();
        roomFutures.clear();
    }
}
