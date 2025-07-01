package com.type_it_backend.services;

import java.util.concurrent.ConcurrentHashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.utils.RandomCodeGenerator;

public class RoomManager {
    private static ConcurrentHashMap<String, Room> activeRooms = new ConcurrentHashMap<String, Room>();

    public static boolean createRoom(Player host) {
        try {
            String roomId = RandomCodeGenerator.generateRandomCode();
            Room room = new Room(roomId, host);
            activeRooms.put(roomId, room);

            return true;

        } catch (Exception e) {
            System.err.println("Error creating room: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteRoom(Room room) {
        String roomId = room.getRoomId();

        if (activeRooms.containsKey(roomId) && RandomCodeGenerator.isExists(roomId)) {
            activeRooms.remove(roomId);
            RandomCodeGenerator.removeCode(roomId);
            return true;
        }

        return false;

    }

    public static boolean isRoomExists(String roomId) {
        return activeRooms.containsKey(roomId);
    }

    public static Room getRoomById(String roomId) {
        return activeRooms.get(roomId);
    }

    public static boolean addPlayerToRoom(String roomId, Player player) {
        Room room = getRoomById(roomId);

        if(room != null){
            room.getPlayers().put(player.getPlayerId(), player); // Add player to the room's player map
            return true;
        }

        return false;
    }

    public static boolean addPlayerToRandomRoom(Player player) {
        for (Room room : activeRooms.values()) {
            if (room.isPublic()) {
                room.getPlayers().put(player.getPlayerId(), player);
                return true; // Player added to a public room
            }
        }
        return false; // No available room found
    }

    public static boolean removePlayerFromRoom(Player player, Room room) {
        try{
            room.getPlayers().remove(player.getPlayerId()); // Remove player from the room's player map
            return true;
        }
        catch (Exception e) {
            System.err.println("Error removing player from room: " + e.getMessage());
            return false;
        }
    }

}
