package com.type_it_backend;

import org.java_websocket.WebSocket;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.data_structure.Player;
import com.type_it_backend.data_structure.Room;

public class GameRoomManager {
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; 
    private static final int CODE_LENGTH = 8; // Length of the game room code
    
    public static void deleteRoom(String room_code, RedisDatabaseManager redis_db_manager) {
        redis_db_manager.deleteData(room_code); // Delete the room data from Redis
    }

    public static String generateRoomCode(RedisDatabaseManager redis_db_manager) {
        Random random = new Random();
        StringBuilder roomCode = new StringBuilder(CODE_LENGTH);

        do{
            for (int i = 0; i < CODE_LENGTH; i++) {
                // Generate a random index to select a character from CODE_CHARACTERS
                int randomIndex = random.nextInt(CODE_CHARACTERS.length());
                roomCode.append(CODE_CHARACTERS.charAt(randomIndex)); // Append the character to the room code
            }
        } while(redis_db_manager.isKeyExist(roomCode.toString())); // Repeat generating if the room code already exists in Redis

        return roomCode.toString(); // Return the generated room code
    }

    public static Room getRoom(String room_code, RedisDatabaseManager redis_db_manager) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String room_data = redis_db_manager.getData(room_code); // Get the room data from Redis
        JsonNode json_room_data = objectMapper.readTree(room_data); // Parse the JSON string to a JsonNode

        if(room_data != null){
            Room room = objectMapper.treeToValue(json_room_data, Room.class); // Convert the JSON string to a Room object
            return room;
        }
        return null; // Return null if the room data is not found
    }

    public static void createRoom(Room room, RedisDatabaseManager redis_db_manager,WebSocket conn,ActiveConnections activeConnections) {

        redis_db_manager.saveData(room.getRoomCode(), room.toJsonString()); // Save the room data to Redis
        activeConnections.addConnection(room.getRoomCode(), conn); // Add the connection to the active connections
    }

    public static boolean isRoomExists(RedisDatabaseManager redis_db_manager,String room_code){
        return redis_db_manager.isKeyExist(room_code); // Return true if the room code exists in Redis, false otherwise
    }

    public static boolean addPlayerToRoom(Room room, Player player,RedisDatabaseManager redis_db_manager) {
        try{
            if(isRoomExists(redis_db_manager, room.getRoomCode())){
                room.appendPlayer(player);
                redis_db_manager.saveData(room.getRoomCode(), room.toJsonString());
                return true;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        
    return false;
    }
}
