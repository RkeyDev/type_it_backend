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
import com.type_it_backend.utils.DatabaseManager;
import com.type_it_backend.utils.ResponseFactory;

public class GameServer extends WebSocketServer {

    public GameServer(int port) {
        super(new java.net.InetSocketAddress(port));
        this.setConnectionLostTimeout(30); // 30 seconds for dead connections
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[OPEN] New connection from: " + conn.getRemoteSocketAddress());
        
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[CLOSE] Connection closed: " + conn.getRemoteSocketAddress() 
            + " | Code: " + code + " | Reason: " + reason + " | Remote: " + remote);

        Player player = RoomManager.getPlayerByConnection(conn);
        if (player == null) {
            System.out.println("[CLOSE] Player not found for this connection.");
            return;
        }

        Room room = player.getRoom();
        if (room == null) {
            System.out.println("[CLOSE] Player is not in any room: " + player.getPlayerName());
            RoomManager.removePlayerFromRoom(player, room);
            return;
        }

        System.out.println("[CLOSE] Player " + player.getPlayerName() + " is leaving room: " + room.getRoomCode());
        System.out.println("[CLOSE] Current players in room before removal: " + room.getPlayersAsString());

        boolean wasHost = player.isHost();

        // Remove player
        RoomManager.removePlayerFromRoom(player, room);
        System.out.println("[CLOSE] Removed player: " + player.getPlayerName() + " | Remaining players: " + room.getPlayersAsString());

        // Delete room if empty
        if (room.getPlayers().isEmpty()) {
            System.out.println("[CLOSE] Room " + room.getRoomCode() + " is empty, deleting room.");
            RoomManager.deleteRoom(room);
            return;
        }

        // Reassign host if needed
        if (wasHost) {
            room.setRandomHost();
            Player newHost = room.getHost();
            if (newHost != null) {
                System.out.println("[CLOSE] New host assigned: " + newHost.getPlayerName());
                newHost.sendResponse(ResponseFactory.newHostResponse(room));
            } else {
                System.out.println("[CLOSE] Failed to assign new host. Room: " + room.getRoomCode());
            }
        }

        // Notify players if in game
        if (room.isInGame()) {
            System.out.println("[CLOSE] Broadcasting playerLeftResponse for " + player.getPlayerName());
            room.broadcastResponse(ResponseFactory.playerLeftResponse(player.getPlayerId(), player.getPlayerName()));

            if (room.haveAllPlayersGuessed()) {
                System.out.println("[CLOSE] All players guessed. Handling new round.");
                NewRoundHandler.handle(room);
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[MESSAGE] Received message: " + message);
        try {
            Request request = Request.stringToRequest(message, conn);
            RequestHandler.handle(request);
        } catch (Exception e) {
            System.out.println("[ERROR] Handling request: " + e.getMessage());
            conn.send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            System.out.println("[ERROR] Connection " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
        } else {
            System.out.println("[ERROR] Server error: " + ex.getMessage());
        }
    }

    @Override
    public void onStart() {
        //Load db
        DatabaseManager.loadAllTables(); //Load all db tables
        DatabaseManager.printPreloadedSummary();
        System.out.println("[START] Server running on port: " + getPort() + " | address: " + getAddress());
    
    }
}
