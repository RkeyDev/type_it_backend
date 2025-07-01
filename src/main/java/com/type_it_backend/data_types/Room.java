package com.type_it_backend.data_types;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class Room{
    private String roomId;
    private Player host;
    private ConcurrentHashMap<String, Player> players;
    private HashSet<Player> currentWinners;
    private boolean isPublic;

    public Room(String roomId, Player host) {
        this.roomId = roomId;
        this.host = host;
        this.players = new ConcurrentHashMap<>();
        this.currentWinners = new HashSet<>();
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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

    public boolean isPublic() {
        return isPublic;
    }


    public ConcurrentHashMap<String, Player> getPlayers() {
        return players;
    }

    public HashSet<Player> getCurrentWinners() {
        return currentWinners;
    }


    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    
}