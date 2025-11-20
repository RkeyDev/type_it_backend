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
import com.type_it_backend.services.ConnectionStore;
import com.type_it_backend.utils.DatabaseManager;
import com.type_it_backend.utils.ResponseFactory;

public class GameServer extends WebSocketServer {

    public GameServer(int port) {
        super(new java.net.InetSocketAddress(port));
        this.setConnectionLostTimeout(30);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Player player = RoomManager.getPlayerByConnection(conn);
        if (player == null) return;

        Room room = player.getRoom();
        if (room == null) {
            RoomManager.removePlayerFromRoom(player, room);
            return;
        }

        ConnectionStore.markDisconnected(player);

        new Thread(() -> {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ignored) {}
            Player p = ConnectionStore.tryResume(null, player.getPlayerId());
            if (p != null) return;

            boolean wasHost = player.isHost();
            RoomManager.removePlayerFromRoom(player, room);

            if (room.getPlayers().isEmpty()) {
                RoomManager.deleteRoom(room);
                return;
            }

            if (wasHost) {
                room.setRandomHost();
                Player newHost = room.getHost();
                if (newHost != null) newHost.sendResponse(ResponseFactory.newHostResponse(room));
            }

            if (room.isInGame()) {
                room.broadcastResponse(ResponseFactory.playerLeftResponse(player.getPlayerId(), player.getPlayerName()));
                if (room.haveAllPlayersGuessed()) NewRoundHandler.handle(room);
            }
        }).start();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("RESUME:")) {
                String id = message.substring(7);
                Player p = ConnectionStore.tryResume(conn, id);
                if (p != null) {
                    p.sendResponse(ResponseFactory.resumeState(p.getRoom()));
                }
                return;
            }

            Request request = Request.stringToRequest(message, conn);
            RequestHandler.handle(request);
        } catch (Exception e) {
            conn.send(ResponseType.REQUEST_HANDLING_ERROR.getResponseType() + ": " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    @Override
    public void onStart() {
        DatabaseManager.loadAllTables();
        DatabaseManager.printPreloadedSummary();
    }
}
