package com.type_it_backend.services;

import java.util.concurrent.ConcurrentHashMap;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Room;

public class RoomManager {
    private static ConcurrentHashMap<String, Room> activeRooms = new ConcurrentHashMap<String, Room>();

    public static boolean createRoom(Player host){
        throw new UnsupportedOperationException("Method not implemented yet");
    }
    
    public static boolean deleteRoom(Room room){
        throw new UnsupportedOperationException("Method not implemented yet");
    }
    
    public static boolean isRoomExists(String roomId){
        return activeRooms.containsKey(roomId);
    }

    public static Room getRoomById(String roomId){
        return activeRooms.get(roomId);
    }

    public static boolean addPlayerToRoom(String roomId, Player player) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public static boolean addPlayerToRandomRoom(Player player) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public static boolean removePlayerFromRoom(Player player, Room room) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    
}
