package com.type_it_backend;
import org.java_websocket.server.WebSocketServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.type_it_backend.data_structure.Room;
import com.type_it_backend.repository.RedisDatabaseManager;
import com.type_it_backend.response.ServerResponse;

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
    private ActiveConnections activeConnections = new ActiveConnections(); // Instance of ActiveConnections to manage connections
    
    public GameServer(int port) {
        //Create a new WebSocket server on the specified port
        super(new InetSocketAddress(port));
        this.redis_db_manager = new RedisDatabaseManager(); // Initialize RedisDatabaseManager
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
        Room room = null; // Initialize room variable

        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance for JSON processing
        
        try{
        // Parse the incoming JSON message
        JsonNode root = objectMapper.readTree(message);
        String typeStr = root.get("type").asText();
        MessageType type = MessageType.fromString(typeStr); // Convert the type string to MessageType enum

        ((ObjectNode) root).remove("type"); // Remove the "type" field from the JSON object

        // Convert the modified JSON object back to a string
        String json_without_type = objectMapper.writeValueAsString(root); 
        root = objectMapper.readTree(json_without_type);


        switch (type) {
            
            case PLAYER_JOIN: // Handle player join
                // Send the client that the player joined successfully
                ServerResponse.playerJoin_Response(root, redis_db_manager, conn, activeConnections);

                // Notify all the players in the room about the new player
                ServerResponse.playerJoinedRoom_Response(room, redis_db_manager, conn, activeConnections, root);
                break;
                

            case ROOM_CREATION: // Handle room creation
                room = objectMapper.treeToValue(root, Room.class);
                GameRoomManager.createRoom(room, redis_db_manager, conn, activeConnections);

                // Notify all the players in the room about the new player
                ServerResponse.playerJoinedRoom_Response(room, redis_db_manager, conn, activeConnections, root);
                break;
                

            case GET_ROOM_CODE:
                ServerResponse.getRomeCode_Response(root, redis_db_manager, conn);
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
        
    }


    public static void main(String[] args) {
       
        int port = 8080;
        GameServer server = new GameServer(port);
        
        server.start(); //Start server

    }
}