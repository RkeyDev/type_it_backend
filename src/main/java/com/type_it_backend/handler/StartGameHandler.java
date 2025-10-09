package com.type_it_backend.handler;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.Language;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;
import com.type_it_backend.utils.SchedulerProvider;

public class StartGameHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        String hostName = (String) data.get("host");

        @SuppressWarnings("unchecked")
        HashMap<String, Object> settings = (HashMap<String, Object>) data.get("settings");

        int typingTime = 0;
        int characterGoal = 0;
        String language = Language.ENGLISH.getLanguage();

        if (settings != null) {
            Object timeObj = settings.get("typingTime");
            Object goalObj = settings.get("characterGoal");
            Object languageObj = settings.get("language");

            if (timeObj instanceof String) {
                try {
                    typingTime = Integer.parseInt((String) timeObj);
                } catch (NumberFormatException e) { }
            }

            if (goalObj instanceof String) {
                try {
                    characterGoal = Integer.parseInt((String) goalObj);
                } catch (NumberFormatException e) { }
            }

            if (languageObj instanceof String) {
                language = (String) languageObj;
            }
        }

        Room room = RoomManager.getRoomByCode(roomCode);

        if (room != null && room.getHost().getPlayerName().equals(hostName)) {
            System.out.println("time: " + typingTime);
            room.setTypingTime(typingTime);
            room.setCharacterGoal(characterGoal);
            room.setLanguage(language);

            room.getPlayers().values().forEach(player -> {
                player.setHasSubmittedCorrectWord(false);
                player.setGussedCharacters(0);
            });

            System.out.println("Starting game...");
            room.broadcastResponse(ResponseFactory.startGameResponse(room));

            SchedulerProvider.SCHEDULER.schedule(() -> {
                if (room.getPlayers().isEmpty()) return;
                room.setInGame(true);
                long serverNow = System.currentTimeMillis();
                int countdownDurationMs = 5000;
                long startAt = serverNow + 250L;
                room.broadcastResponse(ResponseFactory.countdownStartResponse(startAt, countdownDurationMs));
                System.out.println("Broadcasted countdown_start for room " + roomCode + " startAt=" + startAt + " durationMs=" + countdownDurationMs);
                SchedulerProvider.SCHEDULER.schedule(() -> {
                    try {
                        System.out.println("Countdown finished, starting first round for room " + roomCode);
                        NewRoundHandler.handle(room);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, countdownDurationMs + 250L, TimeUnit.MILLISECONDS);
            }, 200L, TimeUnit.MILLISECONDS);
        }
    }
}
