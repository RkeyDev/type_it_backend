package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

        room.addReadyPlayer(); //Count another ready player
        if(room.getReadyPlayer() < room.getPlayers().size())
            return; // Return if not all players are ready yet

        Future<?> oldFuture = roomFutures.remove(roomCode);
        if (oldFuture != null && !oldFuture.isDone()) oldFuture.cancel(true);

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
                CompletableFuture<Void> preloadFuture = CompletableFuture.runAsync(() -> {
                    try {
                        String firstQuestion = DatabaseManager.getRandomQuestion(room.getDatabaseTable());
                        room.setCurrentQuestion(firstQuestion);
                        room.updateAllPossibleAnswers();
                        System.out.println("[Preload] First question ready for room " + roomCode);
                    } catch (Exception e) {
                        System.out.println("[Preload Error] " + e.getMessage());
                    }
                });

                Thread.sleep(6000);

                try {
                    preloadFuture.get(10, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    System.out.println("[Warning] Preload timed out for room " + roomCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (room.getCurrentQuestion() == null || room.getCurrentPossibleAnswers() == null) {
                    System.out.println("[Fallback] Generating question synchronously for room " + roomCode);
                    room.updateCurrentQustion();
                }

                System.out.println("=== Starting First Round instantly for room: " + roomCode + " ===");
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
