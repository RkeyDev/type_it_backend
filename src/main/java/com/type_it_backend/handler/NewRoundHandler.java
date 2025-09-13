package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.JsonFilePath;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class NewRoundHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        JsonFileHandler jsonHandler = new JsonFileHandler(JsonFilePath.WORDS_FILE);
        String randomTopic = jsonHandler.getAllKeys()[(int) (Math.random() * jsonHandler.getAllKeys().length)];

        Room room = RoomManager.getRoomByCode((String) data.get("roomCode"));
        room.setCurrentTopic(randomTopic);

        String question = jsonHandler.getValue(room.getCurrentTopic()).get("question").asText();
        room.broadcastResponse(ResponseFactory.startNewRoundResponse(question));
    }
}
