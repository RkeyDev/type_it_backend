package com.type_it_backend;

import com.type_it_backend.server.GameServer;

public class Main {
    
    public static void main(String[] args) {
        int port = 8080; // Default port

        GameServer server = new GameServer(port);
        server.start();
    }
    
}