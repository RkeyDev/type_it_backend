package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class WordSubmissionHandler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public static void handle(Request request, HashMap<String, Object> data) {
        Room room = RoomManager.getRoomByCode((String) data.get("roomCode"));
        String word = request.getData().get("word").toString().trim();
        Player player = room.getPlayerByConn(request.getSenderConn());
        String question = room.getCurrentQuestion();

        if (player != null && word != null && question != null) {
            List<String> validWordsNode = room.getCurrentPossibleAnswers();

            for (String current_valid_word : validWordsNode) {  // Iterate through valid words

                String validWord = current_valid_word.trim();
                if (word.equalsIgnoreCase(validWord) && !player.hasSubmittedCorrectWord()) {

                    // Correct word submitted
                    player.setHasSubmittedCorrectWord(true);
                    room.addCurrentWinner(player);
                    player.updateGussedCharacters(word);

                    // Correct word response
                    room.broadcastResponse(ResponseFactory.playerGuessedCorrectlyResponse(player, word));

                    // Check if all players have guessed the word
                    if (room.haveAllPlayersGuessed()) {
                        // Clean all schedules before starting new round
                        NewRoundHandler.cleanAllSchedules(room.getRoomCode());

                        scheduler.schedule(() -> {
                            // Handle new round when all players have guessed
                            NewRoundHandler.handleAllPlayersGuessed(room.getRoomCode());
                        }, 1, TimeUnit.SECONDS);
                        
                    }

                    if (player.getGussedCharacters() >= room.getCharacterGoal()){
                        // Player has won the game 
                        room.setInGame(false);
                        room.broadcastResponse(ResponseFactory.playerHasWonResponse(player));
                        

                        // Wait 5 seconds, then bring everyone back to lobby (send return_to_lobby)
                        scheduler.schedule(() -> {
                            room.setInGame(false);
                            room.setCurrentQuestion(null);
                            room.getCurrentWinners().clear();
                            room.getPlayers().values().forEach(current_player -> {
                                current_player.setHasSubmittedCorrectWord(false);
                                current_player.setGussedCharacters(0);
                            });
                            NewRoundHandler.cleanAllSchedules(room.getRoomCode());

                            room.broadcastResponse(ResponseFactory.returnToLobbyResponse(room));
                        }, 5, TimeUnit.SECONDS);
                    }
                    return;
                }
            }
        }
        // Incorrect word submitted
        request.getSenderConn().send(ResponseFactory.playerGuessedIncorrectlyResponse());
    }
}
