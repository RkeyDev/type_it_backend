package com.type_it_backend;

import com.type_it_backend.server.GameServer;

public class Main {

    public static void main(String[] args) {
        String portStr = System.getenv("PORT");  // Host sets port automatically
        int port = (portStr != null) ? Integer.parseInt(portStr) : 8080;

        GameServer server = new GameServer(port);
        server.start();
    }
}
