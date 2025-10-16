package com.type_it_backend;

import com.type_it_backend.server.GameServer;
import com.type_it_backend.utils.DatabaseManager;

public class Main {

    public static void main(String[] args) {
        String portStr = System.getenv("PORT");  // Host sets port automatically
        int port = (portStr != null) ? Integer.parseInt(portStr) : 8080;
        
        //Load db
        DatabaseManager.loadAllTables(); //Load all db tables
        DatabaseManager.printPreloadedSummary();
        
        GameServer server = new GameServer(port);
        server.start();
    }
}
