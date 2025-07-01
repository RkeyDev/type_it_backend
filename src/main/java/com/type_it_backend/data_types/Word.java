package com.type_it_backend.data_types;

import com.type_it_backend.services.RoomManager;

public class Word {
    private String word;
    private String topic;
    private int length;
    private Player player;
    private Room room;


    public Word(String word, String topic, String roomId, String playerId) {
        try{
            this.word = word;
            this.topic = topic;
            this.room = RoomManager.getRoomById(roomId);
            this.player = this.room.getPlayerById(playerId);
        }
        catch (Exception e) {
            System.err.println("Error creating Word: " + e.getMessage());
            throw new IllegalArgumentException("Invalid room or player ID");
        }

    }

    public boolean isValid() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getWord() {
        return word;
    }

    public String getTopic() {
        return topic;
    }

    public int getLength() {
        return length;
    }

    public Player getPlayer() {
        return player;
    }

    public Room getRoom() {
        return room;
    }
    
    
}
