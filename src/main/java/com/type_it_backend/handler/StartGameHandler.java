package com.type_it_backend.handler;

import java.util.HashMap;

import com.type_it_backend.data_types.Request;
import com.type_it_backend.data_types.Room;
import com.type_it_backend.enums.Language;
import com.type_it_backend.services.RoomManager;
import com.type_it_backend.utils.ResponseFactory;

public class StartGameHandler {

    public static void handle(Request request, HashMap<String, Object> data) {
        String roomCode = (String) data.get("roomCode");
        String hostName = (String) data.get("host");

        @SuppressWarnings("unchecked")
        HashMap<String, Object> settings = (HashMap<String, Object>) data.get("settings");

        int typingTime = 0;
        int characterGoal = 0;
        String language = Language.ENGLISH.getLanguage(); // default language

        if (settings != null) {
            Object timeObj = settings.get("typingTime");
            Object goalObj = settings.get("characterGoal");
            Object languageObj = settings.get("language");

            // Parse typingTime
            if (timeObj instanceof String) {
                try {
                    typingTime = Integer.parseInt((String) timeObj);
                } catch (NumberFormatException e) {
                    // Keep default 0
                }
            }

            // Parse characterGoal
            if (goalObj instanceof String) {
                try {
                    characterGoal = Integer.parseInt((String) goalObj);
                } catch (NumberFormatException e) {
                    // Keep default 0
                }
            }

            // Set language
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

            // Reset all players game state
            room.getPlayers().values().forEach(player -> {
                player.setHasSubmittedCorrectWord(false);
                player.setGussedCharacters(0);
            });
            // Start the game (sends start_game response)
            System.out.println("Starting game...");
            room.broadcastResponse(ResponseFactory.startGameResponse(room));
        }
    }
}
