package com.type_it_backend.data_structure;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Room{
    @JsonProperty("room_code")
    private String room_code;
    @JsonProperty("players")
    private List<Player> players;

    public Room() {} // Default constructor for Jackson

    public String getRoomCode() {
        return room_code;
    }

    public void setRoomCode(String room_code) {
        this.room_code = room_code;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void appendPlayer(Player player){
        players.add(player);  
    }

    public String toJsonString() {
        StringBuilder playersJson = new StringBuilder("[");
        for (int i = 0; i < players.size(); i++) {
            playersJson.append(players.get(i).toJsonString());
            if (i < players.size() - 1) {
                playersJson.append(",");
            }
        }
        return "{\"room_code\":\"" + room_code + "\",\"players\":" + playersJson.toString() + "]}";
    }
}