package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;

public class ToggleMatchmakingHandler {
    public static void handle(Request request){
        HashMap<String,Object> data = request.getData();
        Room room = RoomManager.getRoomByCode((String) data.get("roomCode"));
        if (data.get("username").equals(room.getHost().getPlayerName())){
            Boolean isMatchmakingAllowed = ((String) data.get("allow_matchmaking")).equalsIgnoreCase("true");
            room.setAllowMatchmaking(isMatchmakingAllowed); //Set matchmaking to on/off
            System.out.println("Matchmaking for room {" + room.getRoomCode() + "} is set to {" + room.isAllowingMatchmaking() +"}");
        }
    }
}
