package com.type_it_backend;

import org.java_websocket.WebSocket;

import com.type_it_backend.repository.RedisDatabaseManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveConnections extends ConcurrentHashMap<String, LinkedList<WebSocket>> {
    private static final long serialVersionUID = 1L;

    public void addConnection(String roomCode, WebSocket conn) {
        // Add the connection to the list for the room
        // If the room does not exist, create a new room with the connection 
        this.computeIfAbsent(roomCode, k -> new LinkedList<>()).add(conn); 
    }

    public void addConnections(String roomCode, List<WebSocket> connections) {
        
        // Add multiple connections to the list for the room
        this.put(roomCode, (LinkedList<WebSocket>)connections); // Add the room with its connections
    }


    public void removeConnection(String roomCode, WebSocket conn, RedisDatabaseManager redis_db_manager) {
        LinkedList<WebSocket> connections = this.get(roomCode);
        if (connections != null) {
            connections.remove(conn); // Remove the connection from the list
            if (connections.isEmpty()) {
                this.remove(roomCode); // Remove the room if no connections left
                GameRoomManager.deleteRoom(roomCode, redis_db_manager); // Delete the room from Redis
            }
        }
    }
    

    public void sendMessageToRoom(String roomCode, String message) {
        LinkedList<WebSocket> connections = this.get(roomCode);
        if (connections != null) {
            for (WebSocket conn : connections) { // Send message to each connection in the room
                conn.send(message);
            }
        }
    }


    public String toString() {
        StringBuilder string_builder = new StringBuilder();
        for (String roomCode : this.keySet()) {
            string_builder.append("Room Code: ").append(roomCode).append(", Connections: ").append(this.get(roomCode).size()).append("\n");
        }
        return string_builder.toString();
    }
}