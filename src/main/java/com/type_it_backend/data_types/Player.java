package com.type_it_backend.data_types;

import org.java_websocket.WebSocket;

import com.type_it_backend.utils.RandomCodeGenerator;

public class Player {
    private String playerId;
    private String playerName;
    private String playerSkinPath;
    private boolean isHost;
    private WebSocket conn;
    private Room room;
    private int guessedCharacters;
    private boolean hasSubmittedCorrectWord;

    public Player(String playerName, String playerSkinPath, boolean isHost, WebSocket conn) {
        this.playerId = RandomCodeGenerator.generateRandomCode();
        this.playerName = playerName;
        this.playerSkinPath = playerSkinPath;
        this.isHost = isHost;
        this.conn = conn;
        this.hasSubmittedCorrectWord = false;
        this.guessedCharacters = 0;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        this.isHost = host;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean sendResponse(String response) {
        try {
            this.conn.send(response);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send response: " + e.getMessage());
            return false;
        }
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerSkinPath() {
        return playerSkinPath;
    }

    public void setPlayerSkinPath(String playerSkinPath) {
        this.playerSkinPath = playerSkinPath;
    }

    public WebSocket getConn() {
        return conn;
    }

    public void setConn(WebSocket conn) {
        this.conn = conn;
    }

    // ==== Game Logic ====
    public boolean hasSubmittedCorrectWord() {
        return hasSubmittedCorrectWord;
    }

    public void setHasSubmittedCorrectWord(boolean hasSubmittedCorrectWord) {
        this.hasSubmittedCorrectWord = hasSubmittedCorrectWord;
    }

    public int getGuessedCharacters() {
        return guessedCharacters;
    }

    public void setGuessedCharacters(int guessedCharacters) {
        this.guessedCharacters = guessedCharacters;
    }

    public void updateGuessedCharacters(String word) {
        int wordLength = word.replaceAll("\\s+", "").length();
        this.guessedCharacters += wordLength;
    }
}
