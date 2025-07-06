package com.type_it_backend.data_types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Room{
    private String roomCode;
    private Player host;
    private ConcurrentHashMap<String, Player> players;
    private HashSet<Player> currentWinners; // Players who guessed the word correctly in the current round
    private boolean allowMatchmaking;

    public Room(String roomCode, Player host) {
        this.roomCode = roomCode;
        this.host = host;

        this.players = new ConcurrentHashMap<>();
        this.currentWinners = new HashSet<>();
        this.allowMatchmaking = true;
        
        players.put(host.getPlayerId(), host);
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

    public boolean addCurrentWinner(Player player) {
        try{
            currentWinners.add(player); 
        }
        catch (Exception e) {
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

        // Convert each player to a map and add to the list
        for (Player player : players.values()) {
            Map<String, Object> playerMap = new HashMap<>();
            playerMap.put("playerId", player.getPlayerId());
            playerMap.put("username", player.getPlayerName());
            playerMap.put("skinPath", player.getPlayerSkinPath());
            playersList.add(playerMap);
        }

        try {
            return mapper.writeValueAsString(playersList); // Convert the list to a JSON string
        } catch (Exception e) {
            e.printStackTrace();
            return "[]"; // Return an empty JSON array in case of error
        }
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

    
}