package com.type_it_backend;

import java.util.Random;

import com.type_it_backend.data_structure.Player;
import com.type_it_backend.data_structure.Room;

public class GameRoomManager {
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; 
    private static final int CODE_LENGTH = 8; // Length of the game room code


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

    public static void createRoom(Room room, RedisDatabaseManager redis_db_manager) {
        redis_db_manager.saveData(room.getRoomCode(), room.toJsonString()); // Save the room data to Redis
        
    }

    public static boolean isRoomExists(RedisDatabaseManager redis_db_manager,String room_code){
        return redis_db_manager.isKeyExist(room_code);
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
