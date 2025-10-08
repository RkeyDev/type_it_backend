package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class JoinRoomHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = String.valueOf(data.get("roomCode"));

        if (roomCode == null || roomCode.isEmpty()) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Room code cannot be null or empty"));
            return;
        }

        @SuppressWarnings("unchecked")
        HashMap<String, Object> playerData = (HashMap<String, Object>) data.get("player");

        Player player;
        try {
            player = new Player(
                    (String) playerData.get("name"),
                    (String) playerData.get("skinPath"),
                    false,
                    request.getSenderConn()
            );
        } catch (Exception e) {
            request.getSenderConn().send(ResponseFactory.errorResponse("Invalid player data: " + e.getMessage()));
            return;
        }

        Room room = RoomManager.getRoomByCode(roomCode);
        if (room == null) {
            player.getConn().send(ResponseFactory.joinRoomFailedResponse());
            return;
        }

        player.setRoom(room);

        // Try to add player
        try{
        for (Player p :room.getPlayers().values()) {
            if(p.getPlayerName().equals(player.getPlayerName())){
               player.getConn().send(ResponseFactory.joinRoomFailedResponse());
               return; 
            }
        }
    }
    catch(Exception e){
        return;
    }
        Player existing = room.getPlayers().putIfAbsent(player.getPlayerId(), player);

        if (existing != null) {
            player.getConn().send(ResponseFactory.joinRoomFailedResponse());
            return;
        }

        room.broadcastResponse(ResponseFactory.updateRoomResponse(room));
    }
}
