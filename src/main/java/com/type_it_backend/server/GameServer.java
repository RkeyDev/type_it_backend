package com.type_it_backend.server;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class GameServer extends WebSocketServer{

    public GameServer(int port) {
        super(new java.net.InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }


    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }


    @Override
    public void onMessage(WebSocket conn, String message) {

    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        
    }


    @Override
    public void onStart() {
        System.out.println("Server is running on port: " + getPort() + " | address: " + getAddress());

    }

}