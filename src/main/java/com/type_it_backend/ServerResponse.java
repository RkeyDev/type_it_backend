package com.type_it_backend;

import java.util.HashMap;

import org.java_websocket.WebSocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.type_it_backend.data_structure.Player;
import com.type_it_backend.data_structure.Room;

public class ServerResponse {
    public static void playerJoin_Response(JsonNode root,ObjectMapper objectMapper,RedisDatabaseManager redis_db_manager, WebSocket conn,ActiveConnections activeConnections){
        
        // Init a server response hashmap
        HashMap<String,String> serverResponse;

        String connection_status = "failed";
        try{
        Player player = objectMapper.treeToValue(root, Player.class);
        
        if(GameRoomManager.isRoomExists(redis_db_manager, player.getRoomCode())){
            JsonNode player_room_json = objectMapper.readTree(redis_db_manager.getData(player.getRoomCode()));
            Room player_room = objectMapper.treeToValue(player_room_json, Room.class);
            
            //Add player to the room and return true if succeed
            Boolean is_player_added = GameRoomManager.addPlayerToRoom(player_room, player, redis_db_manager);
            connection_status = is_player_added?"connected":"failed";

            if(is_player_added)
                // Add the connection to the active connections
                activeConnections.addConnection(player.getRoomCode(), conn); 
                
        }

        } catch (Exception e) {
            e.printStackTrace();
            connection_status = "failed";
        }

        //Build the server reponse 
        serverResponse = new HashMap<>();
        serverResponse.put("type", "connection_status");
        serverResponse.put("status", connection_status);


        String serverResponseJsonString = hashMapToJsonString(serverResponse); //Convert hashmap to json string
        conn.send(serverResponseJsonString);
    }

    public static void getRomeCode_Response(JsonNode root,ObjectMapper objectMapper,RedisDatabaseManager redis_db_manager, WebSocket conn){
        // Init a server response hashmap
        HashMap<String,String> serverResponse;
        
        // Get the Jedis instance from the RedisDatabaseManager
        String room_code = GameRoomManager.generateRoomCode(redis_db_manager);
        System.out.println(room_code);

        //Build the server reponse  
        serverResponse = new HashMap<>();
        serverResponse.put("type", "room_code");
        serverResponse.put("room_code", room_code);

    
        // Convert hashmap response to a json format
        String serverResponseJson = hashMapToJsonString(serverResponse);
        System.out.println(serverResponseJson);
        
        conn.send(serverResponseJson); // Send the generated room code to the client
    }

    public static String hashMapToJsonString(HashMap<String,String> hashmap){
        ObjectMapper objectMapper = new ObjectMapper();
        String serverResponseJson = null;

        // Convert hashmap response to a json format
        try {
            serverResponseJson = objectMapper.writeValueAsString(hashmap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return serverResponseJson;
    }


    public static void playerJoinedRoom_Response(Room room, RedisDatabaseManager redis_db_manager, WebSocket conn, ActiveConnections activeConnections, JsonNode root) throws JsonProcessingException {
        String room_code = root.get("room_code").asText();
        room = GameRoomManager.getRoom(room_code, redis_db_manager);

        String response = "{\"type\":\"update_player_list\",\"players\":" + room.getPlayersData() + "}";

        activeConnections.sendMessageToRoom(room_code, response); // Send the updated player list to all connections in the room    
    }
}
