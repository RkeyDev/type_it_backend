package com.type_it_backend.services;
import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.ResponseType;
import com.type_it_backend.utils.ResponseBuilder;

public class GameplayManager {
    
    
    public static boolean handleWordSubmission(String word, String topic, Player player, Room room) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public static boolean startGame(Room room) {
        if (room == null || room.getPlayers().isEmpty()) 
            return false;
    
        try {
            HashMap<String, Object> responseMap = new HashMap<>();
            responseMap.put("type", ResponseType.START_GAME.getResponseType());

            room.broadcastResponse(ResponseBuilder.buildResponse(responseMap));

            return true;
        } catch (Exception e) {
            return false;
        }
        
    }

    private boolean startWordGuessing(Room room) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private boolean endWordGuessing(Room room) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private String getRandomTopic() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private boolean hasWon(Player player, Room room){
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    private boolean checkAllPlayersGuessed(Room room) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
