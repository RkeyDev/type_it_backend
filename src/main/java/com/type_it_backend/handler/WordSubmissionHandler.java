package com.type_it_backend.handler;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.JsonFilePath;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class WordSubmissionHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        Room room = RoomManager.getRoomByCode((String) data.get("roomCode"));
        String word = request.getData().get("word").toString().trim();
        Player player = room.getPlayerByConn(request.getSenderConn());
        String topic = room.getCurrentTopic();

        if (word != null && topic != null && player != null) {
            JsonFileHandler jsonHandler = new JsonFileHandler(JsonFilePath.WORDS_FILE);
            JsonNode validWordsNode = jsonHandler.getValue(topic).get("valid_words");

            for (JsonNode node : validWordsNode) {  // Iterate through valid words
                String validWord = node.asText().trim();
                if (word.equalsIgnoreCase(validWord) && !player.hasSubmittedCorrectWord()) {
                    // Correct word submitted
                    player.setHasSubmittedCorrectWord(true);
                    room.addCurrentWinner(player);
                    player.updateGussedCharacters(word);

                    // Correct word response
                    room.broadcastResponse(ResponseFactory.playerGuessedCorrectlyResponse(player, word));

                    if (player.getGussedCharacters()>= room.getCharacterGoal()){
                        // Player has won the game 
                        room.broadcastResponse(ResponseFactory.playerHasWonResponse(player));
                        room.setInGame(false);
                    }
                    return;
                }
            }
        }
        // Incorrect word submitted
        request.getSenderConn().send(ResponseFactory.playerGuessedIncorrectlyResponse());
    }
}
