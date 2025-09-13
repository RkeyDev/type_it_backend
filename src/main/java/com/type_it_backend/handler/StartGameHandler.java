package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class StartGameHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        String hostName = (String) data.get("host");

        @SuppressWarnings("unchecked")
        HashMap<String, Object> settings = (HashMap<String, Object>) data.get("settings");

        int typingTime = 0;
        int characterGoal = 0;

        if (settings != null) {
            Object timeObj = settings.get("typingTime");
            Object goalObj = settings.get("characterGoal");

            if (timeObj instanceof Number number) {
                typingTime = number.intValue();
            }
            if (goalObj instanceof Number number) {
                characterGoal = number.intValue();
            }
        }

        Room room = RoomManager.getRoomByCode(roomCode);

        if (room != null && room.getHost().getPlayerName().equals(hostName)) {
            room.setTypingTime(typingTime);
            room.setCharacterGoal(characterGoal);
            room.broadcastResponse(ResponseFactory.startGameResponse());
        }
    }
}
