package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
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

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final ConcurrentHashMap<String, Future<?>> roomFutures = new ConcurrentHashMap<>();

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        Future<?> oldFuture = roomFutures.remove(roomCode);
        if (oldFuture != null && !oldFuture.isDone()) oldFuture.cancel(true);

        room.setInGame(false);
        room.setCurrentQuestion(null);
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(p -> {
            p.setHasSubmittedCorrectWord(false);
            p.setGussedCharacters(0);
        });

        room.setInGame(true);
        room.broadcastResponse(ResponseFactory.startGameResponse(room));

        Future<?> newFuture = executor.submit(() -> {
            try {
                // === PRELOAD FIRST QUESTION ASYNCHRONOUSLY ===
                CompletableFuture.runAsync(() -> {
                    try {
                        String firstQuestion = DatabaseManager.getRandomQuestion();
                        room.setCurrentQuestion(firstQuestion);
                        room.updateAllPossibleAnswers(); // preload answers early
                        System.out.println("[Preload] First question ready for room " + roomCode);
                    } catch (Exception e) {
                        System.out.println("[Preload Error] " + e.getMessage());
                    }
                });

                // === COUNTDOWN (6s) ===
                Thread.sleep(6000);

                // Ensure the question is ready (blocking check)
                int tries = 0;
                while ((room.getCurrentQuestion() == null || room.getCurrentPossibleAnswers() == null) && tries < 10) {
                    Thread.sleep(100); // wait until preload completes
                    tries++;
                }

                NewRoundHandler.startPreloadedRound(room);

            } catch (InterruptedException e) {
                System.out.println("Game task interrupted for room: " + roomCode);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                roomFutures.remove(room.getRoomCode());
            }
        });

        roomFutures.put(roomCode, newFuture);
    }

    public static void cleanupRoom(String roomCode) {
        Future<?> f = roomFutures.remove(roomCode);
        if (f != null && !f.isDone()) f.cancel(true);
    }

    public static void shutdown() {
        executor.shutdownNow();
        roomFutures.clear();
    }
}
