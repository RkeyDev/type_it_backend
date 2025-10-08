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
        String roomCode = (String) data.get("roomCode");
        Room room = RoomManager.getRoomByCode(roomCode);

        if (room == null) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room not found"));
            return;
        }

        // Reset room
        room.setInGame(false);
        room.setCurrentTopic("");
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(player -> {
            player.setHasSubmittedCorrectWord(false);
            player.setGussedCharacters(0);
        });

        NewRoundHandler.cleanAllSchedules(room.getRoomCode());

        // Send game start info
        room.broadcastResponse(ResponseFactory.startGameResponse(room));

        // Broadcast countdown immediately
        room.broadcastResponse(ResponseFactory.countdownStartResponse(System.currentTimeMillis(), 6000));

        // Schedule game start after countdown
        SchedulerProvider.SCHEDULER.schedule(() -> {
            room.setInGame(true);
            NewRoundHandler.handle(room);
        }, 6, TimeUnit.SECONDS); // 6s countdown
    }
}
