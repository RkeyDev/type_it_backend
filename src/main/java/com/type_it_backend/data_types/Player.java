package com.type_it_backend.data_types;

import java.net.http.WebSocket;

public class Player {
    private String playerId;
    private String playerName;
    private String playerSkinPath;
    private WebSocket conn;

    public Player(String playerId, String playerName, String playerSkinPath, WebSocket conn) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerSkinPath = playerSkinPath;
        this.conn = conn;
    }

    public boolean sendResponse(String response) {
        try{
            this.conn.sendText(response, false);
            return true;
        }
        catch (Exception e) {
            System.err.println("Failed to send response: " + e.getMessage());
            return false;
        }
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setPlayerSkinPath(String playerSkinPath) {
        this.playerSkinPath = playerSkinPath;
    }

    public void setConn(WebSocket conn) {
        this.conn = conn;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlayerSkinPath() {
        return playerSkinPath;
    }

    public WebSocket getConn() {
        return conn;
    }

    
}
