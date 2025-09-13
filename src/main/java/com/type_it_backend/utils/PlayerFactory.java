package com.type_it_backend.utils;

import java.util.HashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.enums.ResponseType;

public class PlayerFactory {

    public static Player fromData(HashMap<String, Object> playerData, boolean isHost, Request request) {
        try {
            return new Player(
                (String) playerData.get("name"),
                (String) playerData.get("skinPath"),
                isHost,
                request.getSenderConn()
            );
        } catch (Exception e) {
            request.getSenderConn().send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
            throw new IllegalArgumentException("Invalid player data: " + e.getMessage());
        }
    }
}
