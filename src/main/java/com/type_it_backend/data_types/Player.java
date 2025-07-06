package com.type_it_backend.data_types;

import org.java_websocket.WebSocket;

import com.type_it_backend.utils.RandomCodeGenerator;

public class Player {
    private String playerId;
    private String playerName;
    private String playerSkinPath;
    private boolean isHost; 
    private WebSocket conn;

    public Player(String playerName, String playerSkinPath, boolean isHost, WebSocket conn) {
        this.playerId = RandomCodeGenerator.generateRandomCode(); // Generate a random player ID
        
        this.playerName = playerName;
        this.playerSkinPath = playerSkinPath;
        this.isHost = isHost; 
        this.conn = conn;
    }


    public boolean sendResponse(String response) {
        try{
            this.conn.send(response);
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

    public boolean getIsHost() {
        return isHost;
    }

    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
    }
    
}
