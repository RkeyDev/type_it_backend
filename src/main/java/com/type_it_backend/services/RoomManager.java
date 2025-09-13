package com.type_it_backend.services;

import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.utils.ResponseFactory;
import com.type_it_backend.utils.RandomCodeGenerator;

public class RoomManager {
    private static final ConcurrentHashMap<String, Room> activeRooms = new ConcurrentHashMap<>();

    public static Room createRoom(Player host) {
        try {
            String roomCode = RandomCodeGenerator.generateRandomCode();
            Room room = new Room(roomCode, host);
            activeRooms.put(roomCode, room);
            host.setRoom(room);
            return room;
        } catch (Exception e) {
            System.err.println("Error creating room: " + e.getMessage());
            return null;
        }
    }

    public static boolean deleteRoom(Room room) {
        String roomCode = room.getRoomCode();

        if (activeRooms.containsKey(roomCode) && RandomCodeGenerator.isExists(roomCode)) {
            activeRooms.remove(roomCode);
            RandomCodeGenerator.removeCode(roomCode);
            return true;
        }
        return false;
    }

    public static boolean isRoomExists(String roomCode) {
        return activeRooms.containsKey(roomCode);
    }

    public static Room getRoomByCode(String roomCode) {
        return activeRooms.get(roomCode);
    }

    public static boolean addPlayerToRoom(String roomCode, Player player) {
        Room room = getRoomByCode(roomCode);
        if (room == null) return false;

        room.getPlayers().put(player.getPlayerId(), player);
        player.setRoom(room);
        room.broadcastResponse(ResponseFactory.updateRoomResponse(room));

        return true;
    }

    public static boolean addPlayerToRandomRoom(Player player) {
        Room[] availableRooms = activeRooms.values().stream()
                .filter(Room::isAllowingMatchmaking)
                .toArray(Room[]::new);

        if (availableRooms.length > 0) {
            Room randomRoom = availableRooms[(int) (Math.random() * availableRooms.length)];
            return addPlayerToRoom(randomRoom.getRoomCode(), player);
        }
        return false;
    }

    public static boolean removePlayerFromRoom(Player player, Room room) {
        try {
            room.getPlayers().remove(player.getPlayerId());
            room.broadcastResponse(ResponseFactory.updateRoomResponse(room));
            return true;
        } catch (Exception e) {
            System.err.println("Error removing player from room: " + e.getMessage());
            return false;
        }
    }

    public static Player getPlayerByConnection(WebSocket conn) {
        for (Room room : activeRooms.values()) {
            for (Player player : room.getPlayers().values()) {
                if (player.getConn().equals(conn)) {
                    return player;
                }
            }
        }
        return null;
    }
}
