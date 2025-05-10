package com.type_it_backend;
import org.java_websocket.server.WebSocketServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.type_it_backend.data_structure.ClientRequest;
import com.type_it_backend.data_structure.Player;
import com.type_it_backend.data_structure.Room;

import redis.clients.jedis.UnifiedJedis;

import java.net.InetSocketAddress;
import java.util.Collections;
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
        System.out.println("Original json string: " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        
        try{
        // Parse the incoming JSON message
        JsonNode root = objectMapper.readTree(message);
        String type = root.get("type").asText();

        ((ObjectNode) root).remove("type"); // Remove the "type" field from the JSON object

        // Convert the modified JSON object back to a string
        String jsonWithoutType = objectMapper.writeValueAsString(root); 
        root = objectMapper.readTree(jsonWithoutType);


        //Check the type of json message
        switch (type) {
            case "user_join": // Handle user join
                Player player = objectMapper.treeToValue(root, Player.class);
                break;
                
            case "room_creation": // Handle room creation
                Room room = objectMapper.treeToValue(root, Room.class);
                GameRoomManager.createRoom(room, redis_db_manager);
                break;
                
            case "getRoomCode":
                // Get the Jedis instance from the RedisDatabaseManager

                conn.send(GameRoomManager.generateRoomCode(redis_db_manager)); // Send the generated room code to the client
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


    public static void main(String[] args) {
       
        int port = 8080;
        GameServer server = new GameServer(port);
        server.start();
        /* 
        

        System.out.println(redis_db_manager.removeData("test_key"));
        */
    }
}