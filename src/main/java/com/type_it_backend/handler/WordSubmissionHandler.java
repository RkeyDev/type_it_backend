package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.JsonFilePath;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;
import com.type_it_backend.utils.SchedulerProvider;

public class WordSubmissionHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        Room room = RoomManager.getRoomByCode((String) data.get("roomCode"));
        if (room == null) return;

        String word = request.getData().get("word").toString().trim();
        Player player = room.getPlayerByConn(request.getSenderConn());
        String topic = room.getCurrentTopic();

        if (word != null && topic != null && player != null) {
            JsonFileHandler jsonHandler = new JsonFileHandler(JsonFilePath.WORDS_FILE);
            JsonNode validWordsNode = jsonHandler.getValue(topic).get("valid_words");

            for (JsonNode node : validWordsNode) {
                String validWord = node.asText().trim();
                if (word.equalsIgnoreCase(validWord) && !player.hasSubmittedCorrectWord()) {
                    // Mark player as correct
                    player.setHasSubmittedCorrectWord(true);
                    room.addCurrentWinner(player);
                    player.updateGussedCharacters(word);

                    // Broadcast correct guess
                    room.broadcastResponse(ResponseFactory.playerGuessedCorrectlyResponse(player, word));

                    // Check if all players guessed correctly
                    if (room.haveAllPlayersGuessed()) {
                        NewRoundHandler.cleanAllSchedules(room.getRoomCode());

                        // Schedule next round immediately (1s delay)
                        SchedulerProvider.SCHEDULER.schedule(() -> {
                            NewRoundHandler.handleAllPlayersGuessed(room.getRoomCode());
                        }, 1, TimeUnit.SECONDS);
                    }

                    // Check if player won the game
                    if (player.getGussedCharacters() >= room.getCharacterGoal()) {
                        room.setInGame(false);
                        room.broadcastResponse(ResponseFactory.playerHasWonResponse(player));

                        // Schedule return to lobby after 5 seconds
                        SchedulerProvider.SCHEDULER.schedule(() -> {
                            room.setInGame(false);
                            room.setCurrentTopic("");
                            room.getCurrentWinners().clear();
                            room.getPlayers().values().forEach(p -> {
                                p.setHasSubmittedCorrectWord(false);
                                p.setGussedCharacters(0);
                            });
                            NewRoundHandler.cleanAllSchedules(room.getRoomCode());
                            room.broadcastResponse(ResponseFactory.returnToLobbyResponse(room));
                        }, 5, TimeUnit.SECONDS);
                    }
                    return;
                }
            }
        }

        // If word is incorrect
        request.getSenderConn().send(ResponseFactory.playerGuessedIncorrectlyResponse());
    }
}
