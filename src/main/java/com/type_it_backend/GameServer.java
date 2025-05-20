package com.type_it_backend;
import org.java_websocket.server.WebSocketServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.type_it_backend.data_structure.Player;
import com.type_it_backend.data_structure.Room;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class GameServer extends WebSocketServer{
    //Set to hold all connections
    private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>()); 
    private RedisDatabaseManager redis_db_manager;


    public GameServer(int port) {
        //Create a new WebSocket server on the specified port
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn); //Add the new connection to the set
        System.out.println("New connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + ":" + conn.getRemoteSocketAddress().getPort());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        ObjectMapper objectMapper = new ObjectMapper();
        
        try{
        // Parse the incoming JSON message
        JsonNode root = objectMapper.readTree(message);
        String type = root.get("type").asText();

        ((ObjectNode) root).remove("type"); // Remove the "type" field from the JSON object

        // Convert the modified JSON object back to a string
        String json_without_type = objectMapper.writeValueAsString(root); 
        root = objectMapper.readTree(json_without_type);


        // Init a server response hashmap
        HashMap<String,String> serverResponse;

        switch (type) {
            
            case "player_join": // Handle player join
                System.out.println("tries top join");
                String connection_status = "failed";
                try{
                Player player = objectMapper.treeToValue(root, Player.class);
                
                if(GameRoomManager.isRoomExists(redis_db_manager, player.getRoomCode())){
                    JsonNode player_room_json = objectMapper.readTree(redis_db_manager.getData(player.getRoomCode()));
                    Room player_room = objectMapper.treeToValue(player_room_json, Room.class);
                
                    //Add player to the room and return true if succeed
                    Boolean is_player_added = GameRoomManager.addPlayerToRoom(player_room, player, redis_db_manager);
                    connection_status = is_player_added?"connected":"failed";
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
                System.out.println(serverResponseJsonString);

                conn.send(serverResponseJsonString);
                break;
                
            case "room_creation": // Handle room creation
                System.out.println("PLayer tries to create a room");
                Room room = objectMapper.treeToValue(root, Room.class);
                GameRoomManager.createRoom(room, redis_db_manager);
                break;
                
            case "get_room_code":
            System.out.println("tries to code room fuck");
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
                break;

            default: // Handle unknown type
                System.out.println("Unknown type: " + type);
        }
    }
        catch (JsonProcessingException e) {
            System.err.println("Error processing JSON: " + e.getMessage());
        }
        
}

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error from " + (conn != null ? conn.getRemoteSocketAddress() : "unknown") + ": " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Server started on port " + getPort());
        redis_db_manager = new RedisDatabaseManager();
    }


    public String hashMapToJsonString(HashMap<String,String> hashmap){
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


    public static void main(String[] args) {
       
        int port = 8080;
        GameServer server = new GameServer(port);
        server.start();
        /* 
        

        System.out.println(redis_db_manager.removeData("test_key"));
        */
    }
}