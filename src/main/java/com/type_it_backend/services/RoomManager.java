package com.type_it_backend.services;

import org.java_websocket.WebSocket;
import java.util.concurrent.ConcurrentHashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.handler.RequestHandler;
import com.type_it_backend.utils.RandomCodeGenerator;

public class RoomManager {
    private static ConcurrentHashMap<String, Room> activeRooms = new ConcurrentHashMap<String, Room>();

    public static Room createRoom(Player host) {
        try {
            String roomCode = RandomCodeGenerator.generateRandomCode();
            Room room = new Room(roomCode, host);
            activeRooms.put(roomCode, room);
            host.setRoom(room); // Set the room for the host player
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

    public static Room getRoomByCode(String roomCode) throws NullPointerException {
        return activeRooms.get(roomCode);
    }

    public static boolean addPlayerToRoom(String roomCode, Player player) {
        Room room = getRoomByCode(roomCode);

        if (room != null) {
            room.getPlayers().put(player.getPlayerId(), player); // Add player to the room's player map
            player.setRoom(room); // Set the room for the player
            return true;
        }

        return false;
    }

    public static Player getPlayerByConnection(WebSocket conn) {
        for (Room room : activeRooms.values()) {
            for (Player player : room.getPlayers().values()) {
                if (player.getConn().equals(conn)) {
                    return player; // Return the player associated with the connection
                }
            }
        }
        return null; // No player found for the given connection
    }

    public static boolean addPlayerToRandomRoom(Player player) {
        for (Room room : activeRooms.values()) {
            if (room.isAllowingMatchmaking()) {
                room.getPlayers().put(player.getPlayerId(), player);
                player.setRoom(room); // Set the room for the player
                return true; // Player added to a public room
            }
        }
        return false; // No available room found
    }

    public static boolean removePlayerFromRoom(Player player, Room room) {
        try {
            room.getPlayers().remove(player.getPlayerId()); // Remove player from the room's player map
            room.broadcastResponse(RequestHandler.updateRoomResponse(room)); // Broadcast updated player list
            return true;
        } catch (Exception e) {
            System.err.println("Error removing player from room: " + e.getMessage());
            return false;
        }
    }

}
