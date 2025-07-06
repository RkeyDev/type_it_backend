package com.type_it_backend.server;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.enums.ResponseType;
import com.type_it_backend.handler.RequestHandler;

public class GameServer extends WebSocketServer{

    public GameServer(int port) {
        super(new java.net.InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + ":" + conn.getRemoteSocketAddress().getPort());
    }


    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() + ":" + conn.getRemoteSocketAddress().getPort() + " | Reason: " + reason);
    }


    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println(message);
        try {
            // Parse the incoming message as a Request object
            Request request = Request.stringToRequest(message, conn);
            RequestHandler.handle(request); // Handle the request using the RequestHandler
        } catch (Exception e) {
            conn.send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
            System.out.println("Error handling request: " + e.getMessage());
        }
    }


    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Error occurred: " + ex.getMessage());
    }


    @Override
    public void onStart() {
        System.out.println("Server is running on port: " + getPort() + " | address: " + getAddress());

    }

}