package com.type_it_backend.data_types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.enums.Language;
import com.type_it_backend.utils.DatabaseManager;

public class Room {
    private String roomCode;
    private Player host;
    private ConcurrentHashMap<String, Player> players;
    private HashSet<Player> currentWinners; // Players who guessed the word correctly in the current round
    private boolean allowMatchmaking;
    private boolean inGame;
    private int typingTime; // Time allocated for typing in seconds
    private int characterGoal;
    private String currentQuestion;
    private Language language;
    private List<String> currentPossibleAnswers;
    private List<String> availableQuestions;

    public Room(String roomCode, Player host) {
        this.roomCode = roomCode;
        this.host = host;
        this.players = new ConcurrentHashMap<>();
        this.currentWinners = new HashSet<>();
        this.allowMatchmaking = false; //Default not allowing matchmaking
        this.characterGoal = 120; // Default character goal
        this.typingTime = 30; // Defualt typing time
        this.currentQuestion = "";
        this.availableQuestions = new ArrayList<>(DatabaseManager.getPreloadedQuestions());
        players.put(host.getPlayerId(), host);
    }


    /**
     * Set a random host from the players list
     */
    public void setRandomHost() {
        if (players == null || players.isEmpty()) return;

        List<String> keys = new ArrayList<>(players.keySet());
        int randomIndex = new Random().nextInt(keys.size());
        String randomKey = keys.get(randomIndex);

        Player newHost = players.get(randomKey);
        this.host = newHost;
    }

    public boolean isInGame() {
        return inGame;
    }

    /**
     * Broadcasts a response to all players in the room.
     * @param response The response to be sent to all players.
     */
    public void broadcastResponse(String response) {
        for (Player player : players.values()) {
            player.sendResponse(response);
        }
    }

    public Player getPlayerById(String playerId) {
        return players.get(playerId);
    }

    public Player getPlayerByConn(WebSocket conn) {
        for (Player player : players.values()) {
            if (player.getConn().equals(conn)) {
                return player;
            }
        }
        return null;
    }

    public boolean addCurrentWinner(Player player) {
        try {
            currentWinners.add(player);
        } catch (Exception e) {
            System.out.println("Error adding current winner: " + e.getMessage());
            return false;
        }
        return true;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setHost(Player host) {
        this.host = host;
    }

    public void setPlayers(ConcurrentHashMap<String, Player> players) {
        this.players = players;
    }

    public void setCurrentWinners(HashSet<Player> currentWinners) {
        this.currentWinners = currentWinners;
    }

    public Player getHost() {
        return host;
    }

    public boolean isAllowingMatchmaking() {
        return allowMatchmaking;
    }

    public String getPlayersAsString() {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> playersList = new ArrayList<>();

        for (Player player : players.values()) {
            Map<String, Object> playerMap = new HashMap<>();
            playerMap.put("playerId", player.getPlayerId());
            playerMap.put("username", player.getPlayerName());
            playerMap.put("skinPath", player.getPlayerSkinPath());
            playersList.add(playerMap);
        }

        try {
            return mapper.writeValueAsString(playersList);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public void setCurrentQuestion(String currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public void updateCurrentQustion() {
        if (this.availableQuestions.isEmpty())
            this.availableQuestions = new ArrayList<>(DatabaseManager.getPreloadedQuestions());

        int index = (int) (Math.random() * this.availableQuestions.size());
        this.currentQuestion = this.availableQuestions.get(index);
        this.availableQuestions.remove(index);
        this.updateAllPossibleAnswers();
    }

    public String getCurrentQuestion() {
        return currentQuestion;
    }

    public void updateAllPossibleAnswers() {
        this.currentPossibleAnswers = new ArrayList<>(DatabaseManager.getPossibleAnswers(currentQuestion));
    }

    public List<String> getCurrentPossibleAnswers() {
        return currentPossibleAnswers;
    }

    public ConcurrentHashMap<String, Player> getPlayers() {
        return players;
    }

    public HashSet<Player> getCurrentWinners() {
        return currentWinners;
    }

    public void setAllowMatchmaking(boolean allowMatchmaking) {
        this.allowMatchmaking = allowMatchmaking;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public int getTypingTime() {
        return typingTime;
    }

    public int getCharacterGoal() {
        return characterGoal;
    }

    public void setTypingTime(int typingTime) {
        this.typingTime = typingTime;
    }

    public void setCharacterGoal(int characterGoal) {
        this.characterGoal = characterGoal;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = Language.valueOf(language.toUpperCase());
    }

    /**
     * Checks if all players in the room have guessed the word correctly.
     * @return true if all players have guessed correctly, false otherwise
     */
    public boolean haveAllPlayersGuessed() {
        for (Player player : players.values()) {
            if (!player.hasSubmittedCorrectWord()) {
                return false;
            }
        }
        return true;
    }
}
