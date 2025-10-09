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

        // Reset room for new game
        room.setInGame(false);
        room.setCurrentTopic("");
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(player -> {
            player.setHasSubmittedCorrectWord(false);
            player.setGussedCharacters(0);
        });
        NewRoundHandler.cleanAllSchedules(room.getRoomCode());

        room.setInGame(true);
        room.broadcastResponse(ResponseFactory.startGameResponse(room));
        

        scheduler.schedule(() -> {
            System.out.println(room);
            System.out.println(room.isInGame());
            System.out.println(RoomManager.isRoomExists(room.getRoomCode()));
            System.out.println("New round is starting...");
            NewRoundHandler.handle(room);
        }, 6, TimeUnit.SECONDS);
    }
}
