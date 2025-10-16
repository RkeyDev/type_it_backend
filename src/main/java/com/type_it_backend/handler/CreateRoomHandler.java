package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.Language;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class CreateRoomHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> playerData = (HashMap<String, Object>) data.get("player");

        Player player;
        try {
            player = new Player(
                    (String) playerData.get("name"),
                    (String) playerData.get("skinPath"),
                    true,
                    request.getSenderConn()
            );
        } catch (Exception e) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Invalid player data: " + e.getMessage()));
            return;
        }

        // Get language from data, default to ENGLISH if missing or invalid
        Language language = Language.ENGLISH;
        if (playerData.containsKey("language")) {
            try {
                language = Language.valueOf(((String) playerData.get("language")).toUpperCase());
            } catch (IllegalArgumentException e) {
                // Keep default ENGLISH if invalid
            }
        }
        Room room = RoomManager.createRoom(player, language);

        if (room == null) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Failed to create room"));
            return;
        }

        player.getConn().send(ResponseFactory.updateRoomResponse(room));
    }
}
