package com.type_it_backend.services;

import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;

import com.type_it_backend.data_types.Player;

public class ConnectionStore {
    private static final Map<String, Player> waiting = new HashMap<>();
    private static final Map<Player, Long> timestamps = new HashMap<>();

    public static void markDisconnected(Player p) {
        waiting.put(p.getPlayerId(), p);
        timestamps.put(p, System.currentTimeMillis());
    }

    public static Player tryResume(WebSocket conn, String playerId) {
        Player p = waiting.get(playerId);
        if (p == null) return null;
        if (System.currentTimeMillis() - timestamps.get(p) > 20000) {
            waiting.remove(playerId);
            timestamps.remove(p);
            return null;
        }
        p.setConn(conn);
        waiting.remove(playerId);
        timestamps.remove(p);
        return p;
    }
}
