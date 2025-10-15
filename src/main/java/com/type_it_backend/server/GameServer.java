package com.type_it_backend.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.type_it_backend.data_types.Player;
import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.ResponseType;
import com.type_it_backend.handler.NewRoundHandler;
import com.type_it_backend.handler.RequestHandler;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class GameServer extends WebSocketServer {

    public GameServer(int port) {
        super(new java.net.InetSocketAddress(port));
        this.setConnectionLostTimeout(30); // 30 seconds for dead connections
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from: " + conn.getRemoteSocketAddress().getAddress().getHostAddress()
                + ":" + conn.getRemoteSocketAddress().getPort());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Connection closed: " + conn.getRemoteSocketAddress() + " | Reason: " + reason);

        Player player = RoomManager.getPlayerByConnection(conn);
        if (player == null) {
            System.out.println("Player not found for closed connection.");
            return;
        }

        Room room = player.getRoom();
        if (room == null) {
            System.out.println("Room not found for disconnected player.");
            RoomManager.removePlayerFromRoom(player, room); // safe cleanup
            return;
        }

        boolean wasHost = player.isIsHost();

        // Remove player first
        RoomManager.removePlayerFromRoom(player, room);
        System.out.println("Removed player: " + player.getPlayerName() + " from room: " + room.getRoomCode());

        // Delete room if empty
        if (room.getPlayers().isEmpty()) {
            System.out.println("Room is empty, deleting room: " + room.getRoomCode());
            RoomManager.deleteRoom(room);
            return;
        }

        // Reassign host if needed
        if (wasHost) {
            room.setRandomHost();
            Player newHost = room.getHost();
            if (newHost != null) {
                System.out.println("New host: " + newHost.getPlayerName());
                newHost.sendResponse(ResponseFactory.newHostResponse(room));
            }
        }

        // Notify players if in game
        if (room.isInGame()) {
            room.broadcastResponse(ResponseFactory.playerLeftResponse(player.getPlayerId(), player.getPlayerName()));

            if (room.haveAllPlayersGuessed()) {
                NewRoundHandler.handle(room);
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message);
        try {
            Request request = Request.stringToRequest(message, conn);
            RequestHandler.handle(request);
        } catch (Exception e) {
            conn.send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
            System.out.println("Error handling request: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            System.out.println("Error on connection " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
        } else {
            System.out.println("Server error: " + ex.getMessage());
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server is running on port: " + getPort() + " | address: " + getAddress());
    }
}
