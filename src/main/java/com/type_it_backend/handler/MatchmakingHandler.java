package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class MatchmakingHandler {

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

        if (RoomManager.addPlayerToRandomRoom(player)) {
            player.getConn().send(ResponseFactory.updateRoomResponse(player.getRoom()));
        } else {
            request.getSenderConn().send(ResponseFactory.matchmakingFailedResponse());
        }
    }
}
