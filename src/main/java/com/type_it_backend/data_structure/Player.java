package com.type_it_backend.data_structure;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Player {
    @JsonProperty("username")
    private String username; 
    @JsonProperty("skin")
    private String skin;
    @JsonProperty("room_code")
    private String room_code;
    @JsonProperty("is_host")
    private Boolean is_host;

    public Player() {} // Default constructor for Jackson

    public String getUsername() {
        return username;
    }

    public String getRoomCode() {
        return room_code;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public void setIsHost(Boolean is_host) {
        this.is_host = is_host;
    }

    public void setRoomCode(String room_code) {
        this.room_code = room_code;
    }



    public String toJsonString() {
        return "{\"username\":\"" + username + "\",\"skin\":\"" + skin + "\",\"room_code\":\"" + room_code + "\",\"is_host\":" + is_host + "}";
    }
}