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

            for (String current_valid_word : validWordsNode) {
                String validWord = current_valid_word.trim();
                if (word.equalsIgnoreCase(validWord) && !player.hasSubmittedCorrectWord()) {
                    handleCorrectGuess(room, player, word);
                    return;
                }
            }
            // Correct word even if typo
            String closest = getClosestMatch(word, validWordsNode, 0.7);
            if (closest != null && !player.hasSubmittedCorrectWord()) {
                handleCorrectGuess(room, player, closest);
                return;
            }
        }

        request.getSenderConn().send(ResponseFactory.playerGuessedIncorrectlyResponse());
    }

    private static void handleCorrectGuess(Room room, Player player, String word) {
        player.setHasSubmittedCorrectWord(true);
        room.addCurrentWinner(player);
        player.updateGussedCharacters(word);
        room.broadcastResponse(ResponseFactory.playerGuessedCorrectlyResponse(player, word));

        if (room.haveAllPlayersGuessed()) {
            NewRoundHandler.cleanAllSchedules(room.getRoomCode());
            scheduler.schedule(() -> NewRoundHandler.handleAllPlayersGuessed(room.getRoomCode()), 1, TimeUnit.SECONDS);
        }

        if (player.getGussedCharacters() >= room.getCharacterGoal()) {
            room.broadcastResponse(ResponseFactory.playerHasWonResponse(player));
            scheduler.schedule(() -> resetRoom(room), 5, TimeUnit.SECONDS);
        }
    }

    private static void resetRoom(Room room) {
        room.setCurrentQuestion(null);
        room.getCurrentWinners().clear();
        room.getPlayers().values().forEach(p -> {
            p.setHasSubmittedCorrectWord(false);
            p.setGussedCharacters(0);
        });
        NewRoundHandler.cleanAllSchedules(room.getRoomCode());
        room.broadcastResponse(ResponseFactory.returnToLobbyResponse(room));
    }

    private static String getClosestMatch(String input, List<String> options, double cutoff) {
        input = input.toLowerCase();
        String bestMatch = null;
        double bestScore = Double.MAX_VALUE;

        for (String option : options) {
            int distance = levenshteinAlgorithm(input, option.toLowerCase());
            double score = (double) distance / Math.max(input.length(), option.length());
            if (score < bestScore) {
                bestScore = score;
                bestMatch = option;
            }
        }

        return (bestScore <= 1 - cutoff) ? bestMatch : null;
    }

    private static int levenshteinAlgorithm(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
