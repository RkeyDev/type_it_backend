package com.type_it_backend.data_types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.enums.DatabaseTable;
import com.type_it_backend.enums.Language;
import com.type_it_backend.utils.DatabaseManager;

public class Room {

    private final int DEFAULT_TYPING_TIME = 30;
    private final int DEFAULT_CHARACTER_GOAL = 120;

    private String roomCode;
    private Player host;
    private ConcurrentHashMap<String, Player> players;
    private HashSet<Player> currentWinners;
    private boolean allowMatchmaking;
    private boolean inGame;
    private int typingTime;
    private int characterGoal;
    private String currentQuestion;
    private Language language;
    private List<String> currentPossibleAnswers;
    private List<String> availableQuestions;

    public Room(String roomCode, Player host, Language language) {
        this.roomCode = roomCode;
        this.host = host;
        this.players = new ConcurrentHashMap<>();
        this.currentWinners = new HashSet<>();
        this.allowMatchmaking = false;
        this.characterGoal = DEFAULT_CHARACTER_GOAL;
        this.typingTime = DEFAULT_TYPING_TIME;
        this.currentQuestion = "";
        this.language = language;
        List<String> preloaded = DatabaseManager.getPreloadedQuestions(this.language);
        this.availableQuestions = (preloaded != null) ? new ArrayList<>(preloaded) : new ArrayList<>();
        this.currentPossibleAnswers = new ArrayList<>();
        players.put(host.getPlayerId(), host);
    }


    public void resetSettings(){
        this.allowMatchmaking = false;
        this.typingTime = DEFAULT_TYPING_TIME;
        this.characterGoal = DEFAULT_CHARACTER_GOAL;    
    }

    public void setRandomHost() {
        if (players == null || players.isEmpty()) return;
        if (players.size() == 1) {
            this.host = players.values().iterator().next();
            return;
        }
        List<String> keys = new ArrayList<>(players.keySet());
        Player newHost;
        do {
            String randomKey = keys.get(new Random().nextInt(keys.size()));
            newHost = players.get(randomKey);
        } while (newHost.equals(this.host) && players.size() > 1);
        this.host = newHost;
    }

    public boolean isInGame() {
        return inGame;
    }

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
            if (player.getConn().equals(conn)) return player;
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
        List<Object> playersList = new ArrayList<>();
        for (Player player : players.values()) {
            var playerMap = new java.util.HashMap<String, Object>();
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
        if (this.availableQuestions == null || this.availableQuestions.isEmpty()) {
            List<String> preloaded = DatabaseManager.getPreloadedQuestions(this.language);
            this.availableQuestions = (preloaded != null) ? new ArrayList<>(preloaded) : new ArrayList<>();
        }
        if (!this.availableQuestions.isEmpty()) {
            int index = new Random().nextInt(this.availableQuestions.size());
            this.currentQuestion = this.availableQuestions.get(index);
            this.availableQuestions.remove(index);
            this.updateAllPossibleAnswers();
        } else {
            this.currentQuestion = null;
            this.currentPossibleAnswers = new ArrayList<>();
        }
    }

    public String getCurrentQuestion() {
        return currentQuestion;
    }

    public void updateAllPossibleAnswers() {
        this.currentPossibleAnswers = new ArrayList<>(
                DatabaseManager.getPossibleAnswers(currentQuestion, this.language.getDatabaseTableName())
        );
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
        return this.language;
    }

    public void setLanguage(String languageStr) {
        if (languageStr == null) return;
        String normalized = languageStr.trim().toUpperCase();
        for (Language lang : Language.values()) {
            if (lang.name().equalsIgnoreCase(normalized) || lang.getLanguage().equalsIgnoreCase(normalized)) {
                this.language = lang;
                List<String> preloaded = DatabaseManager.getPreloadedQuestions(this.language);
                this.availableQuestions = (preloaded != null) ? new ArrayList<>(preloaded) : new ArrayList<>();
                this.currentPossibleAnswers = new ArrayList<>();
                this.currentQuestion = "";
                return;
            }
        }
        System.out.println("[WARN] Unknown language: " + languageStr + " — defaulting to ENGLISH");
        this.language = Language.ENGLISH;
        List<String> preloaded = DatabaseManager.getPreloadedQuestions(this.language);
        this.availableQuestions = (preloaded != null) ? new ArrayList<>(preloaded) : new ArrayList<>();
        this.currentPossibleAnswers = new ArrayList<>();
        this.currentQuestion = "";
    }

    public DatabaseTable getDatabaseTable() {
        return this.language.getDatabaseTableName();
    }

    public boolean haveAllPlayersGuessed() {
        for (Player player : players.values()) {
            if (!player.hasSubmittedCorrectWord()) return false;
        }
        return true;
    }
}
