package com.type_it_backend;
public enum MessageType {
    PLAYER_JOIN("player_join"),
    ROOM_CREATION("room_creation"),
    GET_ROOM_CODE("get_room_code");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
         * Converts a string to the corresponding MessageType enum.
         * @param value The string representation of the message type.
         * @return The corresponding MessageType enum.
    */
    public static MessageType fromString(String value) {
        

        for (MessageType t : values()) { // Iterate through all enum values
            // Check if the type matches the provided value (case-insensitive)
            if (t.type.equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
}
